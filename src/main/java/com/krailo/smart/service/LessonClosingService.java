package com.krailo.smart.service;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.exception.LessonException;
import com.krailo.smart.repository.LessonRepository;
import com.krailo.smart.repository.LessonsStudentsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Проведення уроку: урок + відмітки учнів + списання з балансу. І сторнування уроку.
// Окремий сервіс, бо TransactionStudentService уже залежить від LessonService
// (якби LessonService залежав від TransactionStudentService — було б коло залежностей).
@Transactional
@Service
@AllArgsConstructor
public class LessonClosingService {

    private LessonService lessonService;
    private StudentService studentService;
    private LessonsStudentsRepository lessonsStudentsRepository;
    private LessonRepository lessonRepository;
    private TransactionStudentService transactionStudentService;

    // ВСЕ в одній транзакції: якщо будь-який крок впаде (немає ціни, помилка в БД тощо),
    // у базі не залишиться нічого — ні уроку, ні відміток учнів, ні половини списань.
    // Методи інших сервісів теж @Transactional — вони приєднуються до цієї транзакції.
    public LessonDto closeLesson(LessonDto lessonDto) {
        // 1. урок
        LessonDto lessonDtoWithId = lessonService.create(lessonDto);

        // 2. відмітки учнів (присутність / платний урок)
        // беру з бази урок щоб підтягнуло всю інфо (через кеш першого рівня)
        Lesson lesson = lessonService.findByIdEntity(lessonDtoWithId.getId());
        if (lessonDto.getLessonsStudentsDto() != null) {
            for (LessonsStudentsDto lsdto : lessonDto.getLessonsStudentsDto()) {
                LessonsStudents ls = LessonsStudents.builder()
                        .lesson(lesson)
                        .student(studentService.findByIdEntity(lsdto.getStudentId()))
                        .present(lsdto.isPresent())
                        .payed(lsdto.isPayed())
                        .build();
                lessonsStudentsRepository.saveAndFlush(ls);
            }
        }

        // 3. списання з балансу учнів
        transactionStudentService.createByLesson(lessonDtoWithId);
        return lessonDtoWithId;
    }

    // Сторнування уроку (помилка у формі уроку: не ті учні, не ті відмітки, не та дата) — одна транзакція:
    // повертаємо всі списання учням, позначаємо урок сторнованим і відв'язуємо від слоту розкладу,
    // щоб цей урок можна було провести заново вже правильно.
    public void stornoLesson(Integer lessonId) {
        Lesson lesson = lessonService.findByIdEntity(lessonId);
        if (lesson.isStorned()) {
            throw new LessonException(String.format("Урок від %s уже сторновано", lesson.getDate()));
        }
        // 1. повертаємо гроші учням (записи сторно в журналі)
        transactionStudentService.createStornoByLesson(lesson);
        // 2. урок лишається в історії з позначкою «сторновано»
        lesson.setStorned(true);
        // 3. слот розкладу на цю дату знову вільний (UNIQUE (schedule_id, lesson_date) не заважатиме)
        lesson.setSchedule(null);
        lessonRepository.saveAndFlush(lesson);
    }
}
