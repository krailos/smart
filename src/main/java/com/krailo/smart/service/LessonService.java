package com.krailo.smart.service;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Student;
import com.krailo.smart.exception.LessonException;
import com.krailo.smart.exception.ScheduleConflictException;
import com.krailo.smart.mapper.LessonMapper;
import com.krailo.smart.repository.LessonRepository;
import com.krailo.smart.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
@Service
@AllArgsConstructor
public class LessonService {

    private LessonRepository lessonRepository;
    private LessonMapper lessonMapper;
    private GangService gangService;
    private StudentService studentService;

    public List<LessonDto> findAll() {
        return toDtos(lessonRepository.findAll());
    }

    // уроки вчителя (teacherId null — вчителя не обрано, нічого)
    public List<LessonDto> findAllOfTeacher(Integer teacherId) {
        return teacherId == null ? List.of() : toDtos(lessonRepository.findAllOfTeacher(teacherId));
    }

    private List<LessonDto> toDtos(List<Lesson> lessons) {
        return lessons.stream()
                .peek(l -> l.getLessonsStudents()
                        .sort((o1, o2) -> o1.getStudent().getLastName().compareTo(o2.getStudent().getLastName())))
                .map(lessonMapper::mapEntityToDto).toList();
    }

   public  List<Lesson> findAllByStudentAndBetweenDatesEntity (Student student, LocalDate start, LocalDate end){
       return lessonRepository.findAllByStudentAndBetweenDates(student, start, end);
    }

    public LessonDto findById(Integer id) {
        Lesson lesson = findByIdEntity(id);
        lesson.getLessonsStudents()
                .sort((o1, o2) -> o1.getStudent().getLastName().compareTo(o2.getStudent().getLastName()));
        LessonDto lessonDto = lessonMapper.mapEntityToDto(lesson);
        return lessonDto;
    }

    public Lesson findByIdEntity(Integer id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Урок", id));
    }

    public LessonDto create(LessonDto lessonDto) {
        // урок походить від розкладу
        if (lessonDto.getScheduleId() == null) {
            throw new ScheduleConflictException("Урок можна провести тільки з розкладу");
        }
        // вчителя не вибрали у формі — урок проводить основний вчитель групи
        if (lessonDto.getTeacherId() == null) {
            Gang gang = gangService.findByIdEntity(lessonDto.getGangId());
            if (gang.getTeacher() != null) {
                lessonDto.setTeacherId(gang.getTeacher().getId());
            }
        }
        if (lessonDto.getTeacherId() == null) {
            throw new ScheduleConflictException("Оберіть вчителя уроку");
        }
        validate(lessonDto);
        // один слот розкладу = не більше одного уроку на дату, інакше з учнів спишуть двічі
        // (в БД є ще й обмеження UNIQUE (schedule_id, lesson_date) — на випадок подвійного кліку)
        if (lessonRepository.existsByScheduleIdAndDate(lessonDto.getScheduleId(), lessonDto.getDate())) {
            throw new ScheduleConflictException(
                    String.format("Урок за цим розкладом на %s уже проведено", lessonDto.getDate()));
        }

        Lesson lesson = lessonRepository.saveAndFlush(lessonMapper.mapDtoToEntityForCreate(lessonDto));
        return lessonMapper.mapEntityToDto(lesson);
    }

    public Lesson createEntity(LessonDto lessonDto) {
        return lessonRepository.saveAndFlush(lessonMapper.mapDtoToEntityForCreate(lessonDto));
    }

    public LessonDto update(Integer id, LessonDto lessonDto) {
        Lesson lesson = findByIdEntity(id);
        if (lesson.isStorned()) {
            throw new LessonException("Сторнований урок не редагується — проведіть урок заново з розкладу");
        }
        requireDate(lessonDto);
        // дату змінили — на нову дату з цього ж слоту розкладу не повинно бути іншого уроку (сам урок не рахуємо)
        if (lesson.getSchedule() != null && lessonRepository.existsByScheduleIdAndDateAndIdNot(
                lesson.getSchedule().getId(), lessonDto.getDate(), id)) {
            throw new ScheduleConflictException(
                    String.format("Урок за цим розкладом на %s уже проведено", lessonDto.getDate()));
        }

        lessonMapper.mapDtoToEntityForUpdate(lessonDto, lesson);
        return lessonMapper.mapEntityToDto(lessonRepository.saveAndFlush(lesson));
    }

    // Перевірка введених даних уроку. Конфлікти (зайнятий вчитель, кабінет, група) перевіряються
    // на етапі розкладу — урок походить від розкладу, тут їх не перевіряємо.
    private void validate(LessonDto d) {
        requireDate(d);
        if (d.getStartTime() == null || d.getEndTime() == null || !d.getStartTime().isBefore(d.getEndTime())) {
            throw new ScheduleConflictException("Час завершення уроку має бути пізніше за час початку");
        }
        // відсутній учень не може мати платний урок (учня не було) — суперечлива відмітка, урок не проводимо
        List<String> absentButPaid = d.getLessonsStudentsDto() == null ? List.of() : d.getLessonsStudentsDto().stream()
                .filter(ls -> !ls.isPresent() && ls.isPayed())
                .map(ls -> studentService.findByIdEntity(ls.getStudentId()).getLastName())
                .toList();
        if (!absentButPaid.isEmpty()) {
            throw new LessonException("Відсутній учень не може мати платний урок: " + String.join(", ", absentButPaid)
                    + ". Позначте учня присутнім або зніміть «Платний»");
        }
    }

    private void requireDate(LessonDto d) {
        if (d.getDate() == null) {
            throw new ScheduleConflictException("Вкажіть дату уроку");
        }
    }

    // Уроки не видаляються: помилково проведений урок сторнують (LessonClosingService.stornoLesson)
}