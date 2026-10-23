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
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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


    public ScheduleDto findById(Integer id) {
        return scheduleRepository.findById(id).map(scheduleMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Gang whith id= %d not exist", id)));
    }

    public Schedule findByIdEntity(Integer id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Audience whith id= %d not exist", id)));
    }

    public ScheduleDto create(ScheduleDto scheduleDto) {
        checkConflicts(scheduleDto, 0);

        // Якщо конфліктів немає — зберігаємо
        return Optional.of(scheduleDto).map(scheduleMapper::mapDtoToEntityForCreate).map(scheduleRepository::save)
                .map(scheduleMapper::mapEntityToDto).orElseThrow();
    }

    public ScheduleDto update(Integer id, ScheduleDto scheduleDto) {
        checkConflicts(scheduleDto, id);

        return scheduleRepository.findById(id)
                .map(entity -> scheduleMapper.mapDtoToEntityForUpdate(scheduleDto, entity))
                .map(scheduleRepository::saveAndFlush).map(scheduleMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    public boolean delete(Integer id) {
        return scheduleRepository.findById(id).map(entity -> {
            scheduleRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    // Кидає ScheduleConflictException, якщо в цей час уже зайнятий кабінет, вчитель, група або хтось з учнів групи.
    // excludeId — id розкладу, який редагуємо (для нового розкладу = 0)
    private void checkConflicts(ScheduleDto d, int excludeId) {
        if (d.getStartTime() == null || d.getEndTime() == null || !d.getStartTime().isBefore(d.getEndTime())) {
            throw new ScheduleConflictException("Час завершення має бути пізніше за час початку");
        }

        Gang gang = gangRepository.findById(d.getGangId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("Gang whith id= %d not exist", d.getGangId())));
        Integer teacherId = gang.getTeacher() == null ? null : gang.getTeacher().getId();
        List<Integer> studentIds = students(gang).stream().map(Student::getId).toList();

        // Шукаємо заняття того ж дня, які перетинаються за часом із новим
        List<Schedule> overlappingSchedules = scheduleRepository.findOverlappingSchedules(
                d.getWeekDay(), d.getStartTime(), d.getEndTime(), excludeId);

        // Перевіряємо конфлікти для кожного ресурсу
        for (Schedule existing : overlappingSchedules) {
            String when = String.format("%s %s–%s, група «%s»", existing.getWeekDay(),
                    existing.getStartTime(), existing.getEndTime(), existing.getGang().getName());

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

    private List<Student> students(Gang gang) {
        if (gang.getGangStudents() == null) {
            return List.of();
        }
        return gang.getGangStudents().stream().map(GangsStudents::getStudent).toList();
    }
}
