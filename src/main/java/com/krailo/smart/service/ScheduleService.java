package com.krailo.smart.service;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.GangsStudents;
import com.krailo.smart.entity.Schedule;
import com.krailo.smart.entity.Student;
import com.krailo.smart.exception.ScheduleConflictException;
import com.krailo.smart.mapper.ScheduleMapper;
import com.krailo.smart.repository.GangRepository;
import com.krailo.smart.repository.ScheduleRepository;
import com.krailo.smart.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Transactional
@Service
@AllArgsConstructor
public class ScheduleService {

    private ScheduleRepository scheduleRepository;
    private GangRepository gangRepository;
    private ScheduleMapper scheduleMapper;


    public List<ScheduleDto> findAll() {
      return scheduleRepository.findAll().stream().map(scheduleMapper::mapEntityToDto).toList();
  }

    // розклад вчителя: заняття, які він веде (teacherId null — вчителя не обрано, нічого)
    public List<ScheduleDto> findAllByTeacher(Integer teacherId) {
        if (teacherId == null) {
            return List.of();
        }
        return scheduleRepository.findAllByTeacherId(teacherId).stream().map(scheduleMapper::mapEntityToDto).toList();
    }


    public ScheduleDto findById(Integer id) {
        return scheduleRepository.findById(id).map(scheduleMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Розклад", id));
    }

    public Schedule findByIdEntity(Integer id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Розклад", id));
    }

    public ScheduleDto create(ScheduleDto scheduleDto) {
        checkConflicts(scheduleDto, 0);

        // Якщо конфліктів немає — зберігаємо
        Schedule schedule = scheduleRepository.save(scheduleMapper.mapDtoToEntityForCreate(scheduleDto));
        return scheduleMapper.mapEntityToDto(schedule);
    }

    public ScheduleDto update(Integer id, ScheduleDto scheduleDto) {
        checkConflicts(scheduleDto, id);

        return scheduleRepository.findById(id)
                .map(entity -> scheduleMapper.mapDtoToEntityForUpdate(scheduleDto, entity))
                .map(scheduleRepository::saveAndFlush).map(scheduleMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Розклад", id));
    }

    public boolean delete(Integer id) {
        return scheduleRepository.findById(id).map(entity -> {
            scheduleRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    // Кидає ScheduleConflictException, якщо в цей час уже зайнятий кабінет, вчитель, група або хтось з учнів групи.
    // Якщо вчителя не вибрано — записує в d.teacherId основного вчителя групи.
    // excludeId — id розкладу, який редагуємо (для нового розкладу = 0)
    private void checkConflicts(ScheduleDto d, int excludeId) {
        // порожні поля (форму надіслали в обхід браузера) — зрозуміле повідомлення замість 500
        if (d.getGangId() == null) {
            throw new ScheduleConflictException("Оберіть групу");
        }
        if (d.getSubjectId() == null) {
            throw new ScheduleConflictException("Оберіть курс");
        }
        if (d.getAudienceId() == null) {
            throw new ScheduleConflictException("Оберіть аудиторію");
        }
        if (d.getWeekDay() == null) {
            throw new ScheduleConflictException("Оберіть день тижня");
        }
        if (d.getStartTime() == null || d.getEndTime() == null || !d.getStartTime().isBefore(d.getEndTime())) {
            throw new ScheduleConflictException("Час завершення має бути пізніше за час початку");
        }

        Gang gang = gangRepository.findById(d.getGangId())
                .orElseThrow(() -> new NotFoundException("Група", d.getGangId()));

        // вчителя не вибрали у формі — беремо основного вчителя групи
        if (d.getTeacherId() == null && gang.getTeacher() != null) {
            d.setTeacherId(gang.getTeacher().getId());
        }
        if (d.getTeacherId() == null) {
            throw new ScheduleConflictException("Оберіть вчителя: у групи немає основного вчителя");
        }
        Integer teacherId = d.getTeacherId();
        List<Integer> studentIds = students(gang).stream().map(Student::getId).toList();

        // Шукаємо заняття того ж дня, які перетинаються за часом із новим
        List<Schedule> overlappingSchedules = scheduleRepository.findOverlappingSchedules(
                d.getWeekDay(), d.getStartTime(), d.getEndTime(), excludeId);

        // Перевіряємо конфлікти для кожного ресурсу
        for (Schedule existing : overlappingSchedules) {
            String when = when(existing);

            if (Objects.equals(existing.getAudience().getId(), d.getAudienceId())) {
                throw new ScheduleConflictException("Кабінет уже зайнятий: " + when);
            }
            if (teacherId != null && existing.getTeacher() != null
                    && Objects.equals(existing.getTeacher().getId(), teacherId)) {
                throw new ScheduleConflictException("Вчитель уже має заняття: " + when);
            }
            if (Objects.equals(existing.getGang().getId(), gang.getId())) {
                throw new ScheduleConflictException("Група вже має заняття: " + when);
            }
            // учень може бути в кількох групах — перевіряємо, чи немає спільних учнів
            List<String> busyStudents = students(existing.getGang()).stream()
                    .filter(s -> studentIds.contains(s.getId()))
                    .map(Student::getLastName)
                    .toList();
            if (!busyStudents.isEmpty()) {
                throw new ScheduleConflictException("Учні вже мають заняття (" + String.join(", ", busyStudents) + "): " + when);
            }
        }
    }

    // Перевірка при додаванні учня в групу: учень не може бути на двох заняттях одночасно.
    // Кидає ScheduleConflictException, якщо якийсь слот цієї групи перетинається зі слотом іншої групи учня.
    public void checkStudentFree(Student student, Gang gang) {
        for (Schedule slot : scheduleRepository.findAllByGangId(gang.getId())) {
            List<Schedule> overlapping = scheduleRepository.findOverlappingSchedules(
                    slot.getWeekDay(), slot.getStartTime(), slot.getEndTime(), slot.getId());
            for (Schedule existing : overlapping) {
                boolean studentInThatGang = students(existing.getGang()).stream()
                        .anyMatch(s -> Objects.equals(s.getId(), student.getId()));
                if (studentInThatGang) {
                    throw new ScheduleConflictException(student.getLastName() + " вже має заняття: " + when(existing));
                }
            }
        }
    }

    // "ВІВТОРОК 10:00–11:00, група «Англ-1»" — для повідомлень про конфлікт
    private String when(Schedule schedule) {
        return String.format("%s %s–%s, група «%s»", schedule.getWeekDay(),
                schedule.getStartTime(), schedule.getEndTime(), schedule.getGang().getName());
    }

    private List<Student> students(Gang gang) {
        if (gang.getGangStudents() == null) {
            return List.of();
        }
        return gang.getGangStudents().stream().map(GangsStudents::getStudent).toList();
    }
}
