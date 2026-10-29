package com.krailo.smart.report;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.dto.OpeningBalanceDto;
import com.krailo.smart.dto.PaymentDto;
import com.krailo.smart.entity.*;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.enumeration.WeekDay;
import com.krailo.smart.repository.*;
import com.krailo.smart.service.LessonClosingService;
import com.krailo.smart.service.PaymentReceivingService;
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

// Інтеграційний тест виписки учня: операції створюємо справжніми сервісами (оплата, проведення уроку),
// а звіт має зібрати їх із журналу транзакцій — з початковим балансом на дату звіту.
@SpringBootTest
@ActiveProfiles("test")
class ReportTest {

    private static final LocalDate PAYMENT_DATE = LocalDate.of(2026, 10, 1);
    private static final LocalDate LESSON_DATE = LocalDate.of(2026, 10, 26);

    @Autowired private Report report;
    @Autowired private LessonClosingService lessonClosingService;
    @Autowired private PaymentReceivingService paymentReceivingService;
    @Autowired private StudentRepository studentRepository;
    @Autowired private PriceRepository priceRepository;
    @Autowired private AudienceRepository audienceRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private GangRepository gangRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private PaymentRepository paymentRepository;
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
                .balance(BigDecimal.ZERO)
                .build());
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());

        // оплата 500 від 01.10, урок 26.10 (списано 100) -> баланс учня 400
        paymentReceivingService.receivePayment(new PaymentDto(null, null, student.getId(), new BigDecimal("500"),
                PAYMENT_DATE, "тест", null, null, null));
        lessonClosingService.closeLesson(lessonDto());
    }

    @AfterEach
    void cleanUp() {
        // тест без @Transactional — прибираємо за собою вручну (від залежних таблиць до основних)
        for (String table : List.of("transaction_student", "lessons_students", "lesson", "schedule", "payment",
                "price", "gang", "student", "teacher", "subject", "audience")) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }

    @Test
    void shouldTakePaymentBeforePeriodIntoOpeningBalance() {
        // When: звіт з 20.10 — оплата 01.10 сталася ДО періоду
        Bill bill = report.createBill(student, LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 31));

        // Then: оплата — у початковому балансі, у виписці тільки урок
        assertThat(bill.getOpeningBalance()).isEqualByComparingTo("500");
        assertThat(bill.getTotalPaid()).isZero();
        assertThat(bill.getTotalCharged()).isEqualByComparingTo("100");
        assertThat(bill.getItems()).singleElement()
                .satisfies(item -> {
                    assertThat(item.isPayment()).isFalse();
                    assertThat(item.getDate()).isEqualTo(LESSON_DATE);
                    assertThat(item.getBalance()).isEqualByComparingTo("400");
                });
        // самоперевірка: кінцевий баланс звіту = баланс учня в базі
        assertThat(bill.getClosingBalance()).isEqualByComparingTo("400")
                .isEqualByComparingTo(studentRepository.findById(student.getId()).orElseThrow().getBalance());
    }

    @Test
    void shouldShowPaymentInPeriod_WhenPeriodIncludesIt() {
        // When: звіт з 01.10 — тепер оплата всередині періоду
        Bill bill = report.createBill(student, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        // Then: початковий 0, обидві операції за датою (оплата, потім урок), кінцевий той самий
        assertThat(bill.getOpeningBalance()).isZero();
        assertThat(bill.getTotalPaid()).isEqualByComparingTo("500");
        assertThat(bill.getTotalCharged()).isEqualByComparingTo("100");
        assertThat(bill.getItems()).extracting(ItemBill::isPayment).containsExactly(true, false);
        assertThat(bill.getItems()).extracting(ItemBill::getBalance)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("500"), new BigDecimal("400"));
        assertThat(bill.getClosingBalance()).isEqualByComparingTo("400");
    }

    @Test
    void shouldShowStornoInStatement_AndNetToZero() {
        // Given: урок і оплату сторновано (помилки) — у журналі 4 записи: оплата, урок, сторно уроку, сторно оплати
        lessonClosingService.stornoLesson(lessonRepository.findAll().get(0).getId());
        paymentReceivingService.stornoPayment(paymentRepository.findAll().get(0).getId());

        // When: період з вересня і до кінця (сторно оплати датується днем сторнування — сьогодні)
        Bill bill = report.createBill(student, LocalDate.of(2026, 9, 1), LocalDate.of(2099, 12, 31));

        // Then: усі 4 операції видно (історію не ховаємо), а в підсумку — нуль
        assertThat(bill.getItems()).hasSize(4);
        assertThat(bill.getItems()).extracting(ItemBill::getBalance)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("500"), new BigDecimal("400"), new BigDecimal("500"), BigDecimal.ZERO);
        assertThat(bill.getTotalPaid()).isZero();       // 500 - 500
        assertThat(bill.getTotalCharged()).isZero();    // 100 - 100
        assertThat(bill.getClosingBalance()).isZero()
                .isEqualByComparingTo(studentRepository.findById(student.getId()).orElseThrow().getBalance());
    }

    @Test
    void shouldOrderByOperationDate_WhenLessonEnteredLater() {
        // Given: урок 28.09 провели "заднім числом" — внесли ПІСЛЯ оплати 01.10 і уроку 26.10
        lessonClosingService.closeLesson(lessonDto(LocalDate.of(2026, 9, 28)));

        // When
        Bill bill = report.createBill(student, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31));

        // Then: у виписці — за датою операції, і баланс після кожної рахується в цьому ж порядку
        assertThat(bill.getItems()).extracting(ItemBill::getDate)
                .containsExactly(LocalDate.of(2026, 9, 28), PAYMENT_DATE, LESSON_DATE);
        assertThat(bill.getItems()).extracting(ItemBill::getBalance)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("-100"), new BigDecimal("400"), new BigDecimal("300"));
        assertThat(bill.getTotalCharged()).isEqualByComparingTo("200");
        assertThat(bill.getClosingBalance()).isEqualByComparingTo("300")
                .isEqualByComparingTo(studentRepository.findById(student.getId()).orElseThrow().getBalance());
    }

    @Test
    void shouldTakeOpeningBalanceIntoOpeningBalance_NotIntoPaid() {
        // Given: учень прийшов з боргом 200 на 30.09 (до оплати 01.10 і уроку 26.10)
        paymentReceivingService.receiveOpeningBalance(student.getId(),
                new OpeningBalanceDto(new BigDecimal("-200"), LocalDate.of(2026, 9, 30), null));

        // When: і період, що починається після дати залишку, і період, що її містить
        for (LocalDate start : List.of(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 9, 15))) {
            Bill bill = report.createBill(student, start, LocalDate.of(2026, 10, 31));

            // Then: залишок — у "Початковому балансі", а не в "Сплачено"; рядки — лише оплата й урок
            assertThat(bill.getOpeningBalance()).isEqualByComparingTo("-200");
            assertThat(bill.getTotalPaid()).isEqualByComparingTo("500");
            assertThat(bill.getTotalCharged()).isEqualByComparingTo("100");
            assertThat(bill.getItems()).extracting(ItemBill::getBalance)
                    .usingElementComparator(BigDecimal::compareTo)
                    .containsExactly(new BigDecimal("300"), new BigDecimal("200"));
            // -200 + 500 - 100 = 200 = баланс учня в базі
            assertThat(bill.getClosingBalance()).isEqualByComparingTo("200")
                    .isEqualByComparingTo(studentRepository.findById(student.getId()).orElseThrow().getBalance());
        }
    }

    // урок з розкладу: учень присутній, урок платний
    private LessonDto lessonDto() {
        return lessonDto(LESSON_DATE);
    }

    private LessonDto lessonDto(LocalDate date) {
        return LessonDto.builder()
                .scheduleId(schedule.getId())
                .subjectId(subject.getId())
                .gangId(schedule.getGang().getId())
                .audienceId(schedule.getAudience().getId())
                .teacherId(schedule.getTeacher().getId())
                .date(date)
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .lessonsStudentsDto(List.of(LessonsStudentsDto.builder()
                        .studentId(student.getId()).present(true).payed(true).build()))
                .build();
    }
}
