package com.krailo.smart.report;

import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.repository.LessonRepository;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Звіт «Уроки вчителів» за період — основа для майбутнього нарахування зарплати.
// Рахуються всі проведені уроки, крім сторнованих, — навіть якщо присутніх 0 (вчитель прийшов і чекав).
// Урок зараховується тому, хто його реально провів (при заміні — вчителю-заміні).
@Component
@AllArgsConstructor
public class TeacherLessonsReport {

    private LessonRepository lessonRepository;

    // teacherId null — усі вчителі; результат — вчителі за прізвищем, уроки кожного за датою й часом
    @Transactional(readOnly = true)
    public List<TeacherLessons> create(LocalDate start, LocalDate end, Integer teacherId) {
        Map<Teacher, List<Lesson>> byTeacher = lessonRepository.findAllForTeacherReport(start, end, teacherId).stream()
                .collect(Collectors.groupingBy(Lesson::getTeacher));
        return byTeacher.entrySet().stream()
                .map(e -> toTeacherLessons(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(TeacherLessons::getTeacherName))
                .toList();
    }

    private TeacherLessons toTeacherLessons(Teacher teacher, List<Lesson> lessons) {
        List<LessonRow> rows = lessons.stream()
                .sorted(Comparator.comparing(Lesson::getDate).thenComparing(Lesson::getStartTime))
                .map(this::toRow)
                .toList();
        long minutes = rows.stream().mapToLong(LessonRow::getMinutes).sum();
        return new TeacherLessons(teacher.getLastName() + " " + teacher.getFirstName(), rows, minutes);
    }

    private LessonRow toRow(Lesson l) {
        List<LessonsStudents> students = l.getLessonsStudents();
        int present = (int) students.stream().filter(LessonsStudents::isPresent).count();
        return new LessonRow(l.getDate(), l.getStartTime(), l.getEndTime(),
                l.getSubject() != null ? l.getSubject().getName() : "",
                l.getGang() != null ? l.getGang().getName() : "",
                present, students.size(),
                Duration.between(l.getStartTime(), l.getEndTime()).toMinutes());
    }

    // підсумок одного вчителя
    @Value
    public static class TeacherLessons {
        String teacherName;
        List<LessonRow> lessons;
        long minutes;   // загальна тривалість уроків (у шаблоні — години:хвилини)

        public int getCount() {
            return lessons.size();
        }
    }

    // один урок у деталях
    @Value
    public static class LessonRow {
        LocalDate date;
        LocalTime startTime;
        LocalTime endTime;
        String subjectName;
        String gangName;
        int presentCount;   // присутніх
        int studentsCount;  // усього у списку уроку
        long minutes;
    }
}
