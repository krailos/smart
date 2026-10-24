package com.krailo.smart.service;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.PriceDto;
import com.krailo.smart.entity.*;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.enumeration.WeekDay;
import com.krailo.smart.exception.BusinessException;
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

// Інтеграційний тест архівування (довідники) і м'якого видалення (зв'язки): справжні сервіси + H2.
@SpringBootTest
@ActiveProfiles("test")
class ArchiveTest {

    @Autowired private TeacherService teacherService;
    @Autowired private GangService gangService;
    @Autowired private StudentService studentService;
    @Autowired private ScheduleService scheduleService;
    @Autowired private LessonService lessonService;
    @Autowired private PriceService priceService;

    @Autowired private AudienceRepository audienceRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private GangRepository gangRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private GangsStudentsRepository gangsStudentsRepository;
    @Autowired private PriceRepository priceRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Audience audience;
    private Subject subject;
    private Teacher teacher;
    private Gang gang;

    @BeforeEach
    void setUp() {
        audience = audienceRepository.save(Audience.builder().name("101").build());
        subject = subjectRepository.save(Subject.builder().name("Англійська").build());
        teacher = teacherRepository.save(Teacher.builder().firstName("Ірина").lastName("Шевченко").build());
        gang = gangRepository.save(Gang.builder().name("Англ-1").subject(subject).teacher(teacher).build());
    }

    @AfterEach
    void cleanUp() {
        for (String table : List.of("transaction_student", "lessons_students", "lesson", "schedule", "price",
                "gangs_students", "gang", "student", "teacher", "subject", "audience")) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }

    @Test
    void shouldNotArchiveTeacher_WhileHeIsNeededInFuture() {
        // вчитель веде активну групу — архівувати не можна
        assertThatThrownBy(() -> teacherService.archive(teacher.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("активну групу");

        // групу архівували, але вчитель ще є в розкладі — теж не можна
        gangService.archive(gang.getId());
        scheduleRepository.save(schedule());
        assertThatThrownBy(() -> teacherService.archive(teacher.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("розкладі");
    }

    @Test
    void shouldHideArchivedTeacherFromLists_AndRestore() {
        // Given: вчитель більше ніде не потрібен у майбутньому
        gangService.archive(gang.getId());

        // When
        teacherService.archive(teacher.getId());

        // Then: зник зі списку (і з випадаючих меню форм), з'явився в архіві
        assertThat(teacherService.findAll()).isEmpty();
        assertThat(teacherService.findAllArchived()).singleElement()
                .satisfies(t -> assertThat(t.getLastName()).isEqualTo("Шевченко"));

        // і його можна відновити
        teacherService.restore(teacher.getId());
        assertThat(teacherService.findAll()).hasSize(1);
        assertThat(teacherService.findAllArchived()).isEmpty();
    }

    @Test
    void shouldKeepArchivedTeacherInLessonHistory() {
        // Given: вчитель провів урок, потім пішов зі школи (група і вчитель в архіві)
        lessonRepository.save(Lesson.builder()
                .subject(subject).gang(gang).teacher(teacher).audience(audience)
                .date(LocalDate.of(2026, 10, 19)).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                .build());
        gangService.archive(gang.getId());
        teacherService.archive(teacher.getId());

        // When: дивимось історію уроків
        List<LessonDto> lessons = lessonService.findAll();

        // Then: урок на місці і пам'ятає, хто його провів (з @SoftDelete тут був би null)
        assertThat(lessons).singleElement()
                .satisfies(l -> assertThat(l.getTeacherName()).contains("Шевченко"));
    }

    @Test
    void shouldNotArchiveStudent_WhileInActiveGroup() {
        Student student = studentRepository.save(Student.builder()
                .firstName("Оля").lastName("Петренко").contactName("мама").phone("067")
                .gender(Gender.ДІВЧИНА).studentStatus(StudentStatus.УЧЕНЬ).build());
        gangsStudentsRepository.save(GangsStudents.builder().gang(gang).student(student).build());

        // учень в активній групі — інакше він з'являвся б у формі проведення уроку
        assertThatThrownBy(() -> studentService.archive(student.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("активній групі");

        // група в архіві — тепер учня можна архівувати
        gangService.archive(gang.getId());
        studentService.archive(student.getId());
        assertThat(studentService.findAll()).isEmpty();
        assertThat(studentService.findAllIncludingArchived()).hasSize(1); // для звітів — видно
    }

    @Test
    void shouldSoftDeleteSchedule_AndKeepLessonsWorking() {
        // Given: урок проведено з слоту розкладу
        Schedule slot = scheduleRepository.save(schedule());
        lessonRepository.save(Lesson.builder().schedule(slot)
                .subject(subject).gang(gang).teacher(teacher).audience(audience)
                .date(LocalDate.of(2026, 10, 19)).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                .build());

        // When: слот видалили з розкладу
        scheduleService.delete(slot.getId());

        // Then: слоту не видно, але рядок фізично лишився (is_deleted = true), а урок працює
        assertThat(scheduleService.findAll()).isEmpty();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schedule WHERE is_deleted = true", Integer.class))
                .isEqualTo(1);
        assertThat(lessonService.findAll()).singleElement()
                .satisfies(l -> assertThat(l.getScheduleId()).isNull());
    }

    @Test
    void shouldRejectDuplicateActivePrice_AndHideDeletedPrice() {
        // Given: ціна курсу на 01.09
        priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("100")).date(LocalDate.of(2026, 9, 1)).build());

        // друга активна ціна на ту саму дату — заборонена (перевірка в сервісі замість UNIQUE)
        assertThatThrownBy(() -> priceService.create(PriceDto.builder()
                .subjectId(subject.getId()).value(new BigDecimal("150")).date(LocalDate.of(2026, 9, 1)).build()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("уже є");

        // видалена ціна зникає з курсу (м'яке видалення)
        Integer priceId = priceRepository.findAll().get(0).getId();
        priceService.delete(priceId);
        assertThat(priceRepository.findAll()).isEmpty();
        assertThat(priceRepository.existsBySubjectIdAndDate(subject.getId(), LocalDate.of(2026, 9, 1))).isFalse();
    }

    @Test
    void shouldRejectInvalidPriceValue() {
        // порожня, від'ємна, з копійками понад 2 знаки — сервіс не пропускає (форму можна надіслати в обхід браузера)
        for (BigDecimal bad : new BigDecimal[]{null, new BigDecimal("-1"), new BigDecimal("100.555")}) {
            assertThatThrownBy(() -> priceService.create(PriceDto.builder()
                    .subjectId(subject.getId()).value(bad).date(LocalDate.of(2026, 9, 1)).build()))
                    .isInstanceOf(BusinessException.class);
        }
        assertThat(priceRepository.findAll()).isEmpty();

        // зайві нулі — не копійки: 100.500 = 100.5, така ціна приймається
        priceService.create(PriceDto.builder()
                .subjectId(subject.getId()).value(new BigDecimal("100.500")).date(LocalDate.of(2026, 9, 1)).build());
        assertThat(priceRepository.findAll()).singleElement()
                .satisfies(p -> assertThat(p.getValue()).isEqualByComparingTo("100.5"));
    }

    private Schedule schedule() {
        return Schedule.builder()
                .audience(audience).subject(subject).gang(gang).teacher(teacher)
                .weekDay(WeekDay.ПОНЕДІЛОК)
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                .build();
    }
}
