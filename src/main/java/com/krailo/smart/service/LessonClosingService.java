package com.krailo.smart.service;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.repository.LessonsStudentsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Проведення уроку: урок + відмітки учнів + списання з балансу.
// Окремий сервіс, бо TransactionStudentService уже залежить від LessonService
// (якби LessonService залежав від TransactionStudentService — було б коло залежностей).
@Transactional
@Service
@AllArgsConstructor
public class LessonClosingService {

    private LessonService lessonService;
    private StudentService studentService;
    private LessonsStudentsRepository lessonsStudentsRepository;
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
}
