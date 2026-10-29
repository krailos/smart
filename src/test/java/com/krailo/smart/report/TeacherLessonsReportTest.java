package com.krailo.smart.report;

import com.krailo.smart.entity.*;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Звіт «Уроки вчителів»: хто скільки провів за період — сторновані й поза періодом не рахуються,
// урок без присутніх рахується, урок заміни — тому, хто реально провів
@SpringBootTest
@ActiveProfiles("test")
class TeacherLessonsReportTest {

    private static final LocalDate START = LocalDate.of(2026, 10, 1);
    private static final LocalDate END = LocalDate.of(2026, 10, 31);

    @Autowired private TeacherLessonsReport teacherLessonsReport;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private GangRepository gangRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private LessonsStudentsRepository lessonsStudentsRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Teacher shevchenko;
    private Teacher bondar;
    private Subject subject;
    private Gang gang;

    @BeforeEach
    void setUp() {
        shevchenko = teacherRepository.save(Teacher.builder().firstName("Ірина").lastName("Шевченко").build());
        bondar = teacherRepository.save(Teacher.builder().firstName("Олег").lastName("Бондар").build());
        subject = subjectRepository.save(Subject.builder().name("Англійська").build());
        gang = gangRepository.save(Gang.builder().name("Англ-1").subject(subject).teacher(shevchenko).build());
        Student olya = student("Оля");
        Student ivan = student("Іван");

        // Шевченко: урок 90 хв (1 з 2 присутній) і урок 60 хв без присутніх
        Lesson first = lesson(shevchenko, LocalDate.of(2026, 10, 5), 10, 0, 11, 30, false);
        lessonsStudentsRepository.save(LessonsStudents.builder().lesson(first).student(olya).present(true).payed(true).build());
        lessonsStudentsRepository.save(LessonsStudents.builder().lesson(first).student(ivan).present(false).payed(true).build());
        Lesson empty = lesson(shevchenko, LocalDate.of(2026, 10, 12), 10, 0, 11, 0, false);
        lessonsStudentsRepository.save(LessonsStudents.builder().lesson(empty).student(olya).present(false).payed(true).build());
        // не рахуються: сторнований і поза періодом
        lesson(shevchenko, LocalDate.of(2026, 10, 19), 10, 0, 11, 0, true);
        lesson(shevchenko, LocalDate.of(2026, 11, 2), 10, 0, 11, 0, false);
        // заміна: групу Шевченко провів Бондар — урок його
        lesson(bondar, LocalDate.of(2026, 10, 26), 10, 0, 11, 0, false);
    }

    @AfterEach
    void cleanUp() {
        for (String table : List.of("lessons_students", "lesson", "gang", "student", "teacher", "subject")) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }

    @Test
    void shouldCountConductedLessonsPerTeacher() {
        List<TeacherLessonsReport.TeacherLessons> result = teacherLessonsReport.create(START, END, null);

        // за прізвищем: Бондар, Шевченко
        assertThat(result).extracting(TeacherLessonsReport.TeacherLessons::getTeacherName)
                .containsExactly("Бондар Олег", "Шевченко Ірина");

        TeacherLessonsReport.TeacherLessons bondarLessons = result.get(0);
        assertThat(bondarLessons.getCount()).isEqualTo(1);
        assertThat(bondarLessons.getMinutes()).isEqualTo(60);

        TeacherLessonsReport.TeacherLessons shevchenkoLessons = result.get(1);
        assertThat(shevchenkoLessons.getCount()).isEqualTo(2);
        assertThat(shevchenkoLessons.getMinutes()).isEqualTo(150);
        assertThat(shevchenkoLessons.getLessons()).extracting(TeacherLessonsReport.LessonRow::getDate)
                .containsExactly(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 12));
        TeacherLessonsReport.LessonRow row = shevchenkoLessons.getLessons().get(0);
        assertThat(row.getPresentCount()).isEqualTo(1);
        assertThat(row.getStudentsCount()).isEqualTo(2);
        assertThat(row.getSubjectName()).isEqualTo("Англійська");
        assertThat(row.getGangName()).isEqualTo("Англ-1");
        assertThat(shevchenkoLessons.getLessons().get(1).getPresentCount()).isZero();
    }

    @Test
    void shouldFilterByTeacher() {
        assertThat(teacherLessonsReport.create(START, END, bondar.getId()))
                .singleElement()
                .satisfies(t -> assertThat(t.getTeacherName()).isEqualTo("Бондар Олег"));
    }

    private Student student(String firstName) {
        return studentRepository.save(Student.builder()
                .firstName(firstName).lastName("Петренко")
                .contactName("мама").phone("0670000000")
                .gender(Gender.ДІВЧИНА).studentStatus(StudentStatus.УЧЕНЬ)
                .balance(BigDecimal.ZERO)
                .build());
    }

    private Lesson lesson(Teacher teacher, LocalDate date, int h1, int m1, int h2, int m2, boolean storned) {
        return lessonRepository.save(Lesson.builder()
                .teacher(teacher).subject(subject).gang(gang).date(date)
                .startTime(LocalTime.of(h1, m1)).endTime(LocalTime.of(h2, m2))
                .storned(storned)
                .build());
    }
}
