package com.krailo.smart.service;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.entity.*;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.enumeration.WeekDay;
import com.krailo.smart.exception.LessonException;
import com.krailo.smart.exception.PriceNotFoundException;
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
    @Autowired private DiscountRepository discountRepository;
    @Autowired private StudentsDiscountsRepository studentsDiscountsRepository;
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
    }

    @AfterEach
    void cleanUp() {
        // тест без @Transactional — прибираємо за собою вручну (від залежних таблиць до основних)
        for (String table : List.of("transaction_student", "lessons_students", "lesson", "schedule",
                "price", "students_discounts", "discount", "gang", "student", "teacher", "subject", "audience")) {
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
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());

        // When
        lessonClosingService.closeLesson(lessonDto());

        // Then: збережено все разом — урок (з розкладом), відмітка учня, списання і новий баланс
        assertThat(lessonRepository.findAll()).singleElement()
                .satisfies(l -> assertThat(l.getSchedule().getId()).isEqualTo(schedule.getId()));
        assertThat(lessonsStudentsRepository.count()).isEqualTo(1);
        assertThat(transactionStudentRepository.count()).isEqualTo(1);
        assertThat(balance()).isEqualByComparingTo("-100");
    }

    @Test
    void shouldRoundDiscountToKopiyka_HalfUp() {
        // Given: ціна 149.50, у учня знижка 7 % на цей курс
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("149.50")).date(LocalDate.of(2026, 9, 1)).build());
        Discount discount = discountRepository.save(Discount.builder().name("Сімейна").value(7).date(LocalDate.of(2026, 9, 1)).build());
        studentsDiscountsRepository.save(StudentsDiscounts.builder()
                .student(student).subject(subject).discount(discount).date(LocalDate.of(2026, 9, 1)).build());

        // When
        lessonClosingService.closeLesson(lessonDto());

        // Then: 149.50 × 7 % = 10.465 -> 10.47 (шкільне округлення), списано 149.50 - 10.47 = 139.03
        assertThat(transactionStudentRepository.findAll()).singleElement()
                .satisfies(t -> {
                    assertThat(t.getDiscountSum()).isEqualByComparingTo("10.47");
                    assertThat(t.getCredit()).isEqualByComparingTo("139.03");
                });
        assertThat(balance()).isEqualByComparingTo("-139.03");
    }

    @Test
    void shouldReturnMoneyAndAllowClosingAgain_WhenLessonStorned() {
        // Given: ціна 100; урок проведено (з учня списано 100)
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());
        LessonDto held = lessonClosingService.closeLesson(lessonDto());
        assertThat(balance()).isEqualByComparingTo("-100");

        // When: виявилась помилка у формі — сторнуємо урок
        lessonClosingService.stornoLesson(held.getId());

        // Then: гроші повернуто, урок лишився в історії з позначкою і відв'язаний від розкладу
        assertThat(balance()).isZero();
        Lesson storned = lessonRepository.findById(held.getId()).orElseThrow();
        assertThat(storned.isStorned()).isTrue();
        assertThat(storned.getSchedule()).isNull();
        assertThat(transactionStudentRepository.count()).isEqualTo(2); // списання + сторно

        // і цей урок можна провести заново на ту саму дату
        lessonClosingService.closeLesson(lessonDto());
        assertThat(lessonRepository.count()).isEqualTo(2);
        assertThat(balance()).isEqualByComparingTo("-100");
    }

    @Test
    void shouldNotStornoLessonTwice() {
        // Given
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());
        LessonDto held = lessonClosingService.closeLesson(lessonDto());
        lessonClosingService.stornoLesson(held.getId());

        // When & Then: повторне сторно заборонене, гроші вдруге не повертаються
        assertThatThrownBy(() -> lessonClosingService.stornoLesson(held.getId()))
                .isInstanceOf(LessonException.class)
                .hasMessageContaining("уже сторновано");
        assertThat(balance()).isZero();
    }

    @Test
    void shouldNotCloseLesson_WhenAbsentStudentIsMarkedPaid() {
        // Given: ціна є; учень відсутній, але стоїть "платний" — суперечлива відмітка
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());

        // When & Then: урок не проводиться, у повідомленні — прізвище учня
        assertThatThrownBy(() -> lessonClosingService.closeLesson(lessonDto(false, true)))
                .isInstanceOf(LessonException.class)
                .hasMessageContaining("Відсутній учень не може мати платний урок")
                .hasMessageContaining("Петренко");

        // нічого не збережено
        assertThat(lessonRepository.count()).isZero();
        assertThat(lessonsStudentsRepository.count()).isZero();
        assertThat(transactionStudentRepository.count()).isZero();
        assertThat(balance()).isZero();

        // відсутній і безкоштовний — звичайна ситуація, урок проводиться без списання
        lessonClosingService.closeLesson(lessonDto(false, false));
        assertThat(lessonRepository.count()).isEqualTo(1);
        assertThat(balance()).isZero();
    }

    @Test
    void shouldChargePriceValidOnLessonDate_WhenPriceChanged() {
        // Given: ціна курсу змінювалась — 100 з 01.09, 120 з 01.10, і вже внесена майбутня 150 з 01.11
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("120")).date(LocalDate.of(2026, 10, 1)).build());
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("150")).date(LocalDate.of(2026, 11, 1)).build());

        // When: урок 28.09 (діяла ціна 100) і урок 26.10 (діє 120; майбутня 150 ще не настала)
        lessonClosingService.closeLesson(lessonDto(LocalDate.of(2026, 9, 28), true, true));
        lessonClosingService.closeLesson(lessonDto(LESSON_DATE, true, true));

        // Then: кожен урок — за ціною, що діяла саме на його дату
        assertThat(transactionStudentRepository.findAll())
                .extracting(TransactionStudent::getCredit)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactlyInAnyOrder(new BigDecimal("100"), new BigDecimal("120"));
        assertThat(balance()).isEqualByComparingTo("-220");
    }

    @Test
    void shouldApplyOnlyDiscountOfLessonSubjectValidOnLessonDate() {
        // Given: ціна 100; у учня кілька знижок
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());
        Subject drawing = subjectRepository.save(Subject.builder().name("Малювання").build());
        discount(drawing, 50, LocalDate.of(2026, 9, 1));    // інший курс — не має зачепити англійську
        discount(subject, 5, LocalDate.of(2026, 8, 1));     // давня знижка цього курсу
        discount(subject, 20, LocalDate.of(2026, 9, 1));    // новіша — вона й діє на 26.10
        discount(subject, 30, LocalDate.of(2026, 11, 1));   // майбутня — ще не діє

        // When
        lessonClosingService.closeLesson(lessonDto());

        // Then: 20 % від 100 = 20, списано 80
        assertThat(transactionStudentRepository.findAll()).singleElement()
                .satisfies(t -> {
                    assertThat(t.getDiscountPercentage()).isEqualTo(20);
                    assertThat(t.getDiscountSum()).isEqualByComparingTo("20");
                    assertThat(t.getCredit()).isEqualByComparingTo("80");
                });
        assertThat(balance()).isEqualByComparingTo("-80");
    }

    @Test
    void shouldNotCharge_WhenPresentButLessonNotPaid() {
        // Given: ціна є; учень присутній, але урок безкоштовний (не відмічено «Платний»)
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());

        // When
        lessonClosingService.closeLesson(lessonDto(true, false));

        // Then: урок проведено, але нічого не списано
        assertThat(lessonRepository.count()).isEqualTo(1);
        assertThat(transactionStudentRepository.findAll())
                .allSatisfy(t -> assertThat(t.getCredit()).isZero());
        assertThat(balance()).isZero();
    }

    @Test
    void shouldNotApplyDeletedStudentDiscount() {
        // Given: ціна 100; знижку 20 % учню призначили, а потім видалили (м'яке видалення)
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());
        StudentsDiscounts deleted = discount(student, subject, 20, LocalDate.of(2026, 9, 1));
        studentsDiscountsRepository.delete(deleted);

        // When
        lessonClosingService.closeLesson(lessonDto());

        // Then: видалена знижка не діє — списано повну ціну
        assertThat(transactionStudentRepository.findAll()).singleElement()
                .satisfies(t -> {
                    assertThat(t.getDiscountPercentage()).isZero();
                    assertThat(t.getCredit()).isEqualByComparingTo("100");
                });
    }

    @Test
    void shouldApplyDiscountFromLessonDate_ButNotFromNextDay() {
        // Given: ціна 100; знижка 20 % — рівно з дати уроку, 30 % — з наступного дня
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());
        discount(subject, 20, LESSON_DATE);
        discount(subject, 30, LESSON_DATE.plusDays(1));

        // When
        lessonClosingService.closeLesson(lessonDto());

        // Then: межа включно — 20 % вже діє, 30 % ще ні
        assertThat(transactionStudentRepository.findAll()).singleElement()
                .satisfies(t -> assertThat(t.getCredit()).isEqualByComparingTo("80"));
    }

    @Test
    void shouldChargeEachStudentWithOwnDiscount() {
        // Given: ціна 100; Оля зі знижкою 10 %, Іван без знижки — обидва на одному уроці
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());
        discount(subject, 10, LocalDate.of(2026, 9, 1));
        Student ivan = studentRepository.save(Student.builder()
                .firstName("Іван").lastName("Коваль")
                .contactName("тато Іван").phone("0500000000")
                .gender(Gender.ХЛОПЕЦЬ).studentStatus(StudentStatus.УЧЕНЬ)
                .balance(BigDecimal.ZERO)
                .build());
        LessonDto lesson = lessonDto();
        lesson.setLessonsStudentsDto(List.of(
                LessonsStudentsDto.builder().studentId(student.getId()).present(true).payed(true).build(),
                LessonsStudentsDto.builder().studentId(ivan.getId()).present(true).payed(true).build()));

        // When
        lessonClosingService.closeLesson(lesson);

        // Then: знижка Олі не зачепила Івана
        assertThat(balance()).isEqualByComparingTo("-90");
        assertThat(studentRepository.findById(ivan.getId()).orElseThrow().getBalance()).isEqualByComparingTo("-100");
    }

    @Test
    void shouldReturnDiscountedSum_WhenDiscountedLessonStorned() {
        // Given: ціна 100, знижка 20 % — за урок списано 80
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());
        discount(subject, 20, LocalDate.of(2026, 9, 1));
        LessonDto held = lessonClosingService.closeLesson(lessonDto());
        assertThat(balance()).isEqualByComparingTo("-80");

        // When
        lessonClosingService.stornoLesson(held.getId());

        // Then: повернуто саме 80 (а не 100) — баланс знову 0; у сторно сума і знижка з мінусом
        assertThat(balance()).isZero();
        assertThat(transactionStudentRepository.findAll())
                .extracting(TransactionStudent::getCredit)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactlyInAnyOrder(new BigDecimal("80"), new BigDecimal("-80"));
    }

    private StudentsDiscounts discount(Subject forSubject, int percent, LocalDate from) {
        return discount(student, forSubject, percent, from);
    }

    private StudentsDiscounts discount(Student forStudent, Subject forSubject, int percent, LocalDate from) {
        Discount discount = discountRepository.save(Discount.builder()
                .name("Знижка " + percent + "% з " + from).value(percent).date(from).build());
        return studentsDiscountsRepository.save(StudentsDiscounts.builder()
                .student(forStudent).subject(forSubject).discount(discount).date(from).build());
    }

    private BigDecimal balance() {
        return studentRepository.findById(student.getId()).orElseThrow().getBalance();
    }

    // урок з розкладу: учень присутній, урок платний
    private LessonDto lessonDto() {
        return lessonDto(true, true);
    }

    private LessonDto lessonDto(boolean present, boolean payed) {
        return lessonDto(LESSON_DATE, present, payed);
    }

    // той самий слот розкладу, але на іншу дату (урок за цим слотом — один на дату)
    private LessonDto lessonDto(LocalDate date, boolean present, boolean payed) {
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
                        .studentId(student.getId()).present(present).payed(payed).build()))
                .build();
    }
}
