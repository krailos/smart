package com.krailo.smart.service;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.entity.*;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.enumeration.WeekDay;
import com.krailo.smart.exception.PriceNotFoundException;
import com.krailo.smart.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Інтеграційний тест: справжні сервіси + база H2 (без моків),
// бо перевіряємо саме транзакцію — чи відкочується ВСЕ разом.
// Клас навмисно БЕЗ @Transactional: інакше тест сам обгорнув би все в транзакцію
// і ми б не побачили, що реально збереглось у базі.
@SpringBootTest
@ActiveProfiles("test")
class LessonClosingServiceTest {

    private static final LocalDate LESSON_DATE = LocalDate.of(2026, 10, 26);

    @Autowired private LessonClosingService lessonClosingService;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private LessonsStudentsRepository lessonsStudentsRepository;
    @Autowired private TransactionStudentRepository transactionStudentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private PriceRepository priceRepository;
    @Autowired private AudienceRepository audienceRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private GangRepository gangRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Subject subject;
    private Schedule schedule;
    private Student student;

    @BeforeEach
    void setUp() {
        Audience audience = audienceRepository.save(Audience.builder().name("101").build());
        subject = subjectRepository.save(Subject.builder().name("Англійська").build());
        Teacher teacher = teacherRepository.save(Teacher.builder().firstName("Ірина").lastName("Шевченко").build());
        Gang gang = gangRepository.save(Gang.builder().name("Англ-1").subject(subject).teacher(teacher).build());
        schedule = scheduleRepository.save(Schedule.builder()
                .audience(audience).subject(subject).gang(gang).teacher(teacher)
                .weekDay(WeekDay.ПОНЕДІЛОК)
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                .build());
        student = studentRepository.save(Student.builder()
                .firstName("Оля").lastName("Петренко")
                .contactName("мама Оля").phone("0670000000")
                .gender(Gender.ДІВЧИНА).studentStatus(StudentStatus.УЧЕНЬ)
                .balance(0)
                .build());
    }

    @AfterEach
    void cleanUp() {
        // тест без @Transactional — прибираємо за собою вручну (від залежних таблиць до основних)
        for (String table : List.of("transaction_student", "lessons_students", "lesson", "schedule",
                "price", "gang", "student", "teacher", "subject", "audience")) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }

    @Test
    void shouldSaveNothing_WhenNoPriceForLessonDate() {
        // Given: у курсу немає ціни на дату уроку, учень присутній на платному уроці

        // When: проводимо урок — списання впаде на пошуку ціни
        assertThatThrownBy(() -> lessonClosingService.closeLesson(lessonDto()))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessageContaining("немає ціни");

        // Then: відкотилось УСЕ — ні уроку, ні відміток учнів, ні списань, баланс не змінився
        assertThat(lessonRepository.count()).isZero();
        assertThat(lessonsStudentsRepository.count()).isZero();
        assertThat(transactionStudentRepository.count()).isZero();
        assertThat(studentRepository.findById(student.getId()).orElseThrow().getBalance()).isZero();
    }

    @Test
    void shouldSaveLessonStudentsAndCharge_WhenPriceExists() {
        // Given: ціна курсу 100 діє з 01.09
        priceRepository.save(Price.builder().subject(subject).value(100).date(LocalDate.of(2026, 9, 1)).build());

        // When
        lessonClosingService.closeLesson(lessonDto());

        // Then: збережено все разом — урок (з розкладом), відмітка учня, списання і новий баланс
        assertThat(lessonRepository.findAll()).singleElement()
                .satisfies(l -> assertThat(l.getSchedule().getId()).isEqualTo(schedule.getId()));
        assertThat(lessonsStudentsRepository.count()).isEqualTo(1);
        assertThat(transactionStudentRepository.count()).isEqualTo(1);
        assertThat(studentRepository.findById(student.getId()).orElseThrow().getBalance()).isEqualTo(-100);
    }

    // урок з розкладу: учень присутній, урок платний
    private LessonDto lessonDto() {
        return LessonDto.builder()
                .scheduleId(schedule.getId())
                .subjectId(subject.getId())
                .gangId(schedule.getGang().getId())
                .audienceId(schedule.getAudience().getId())
                .teacherId(schedule.getTeacher().getId())
                .date(LESSON_DATE)
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .lessonsStudentsDto(List.of(LessonsStudentsDto.builder()
                        .studentId(student.getId()).present(true).payed(true).build()))
                .build();
    }
}
