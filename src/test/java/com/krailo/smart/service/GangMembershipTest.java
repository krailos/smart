package com.krailo.smart.service;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.*;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.enumeration.WeekDay;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.exception.ScheduleConflictException;
import com.krailo.smart.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Інтеграційний тест складу груп і конфліктів учнів у розкладі: справжні сервіси + H2, без заглушок.
// Учень не може бути на двох заняттях одночасно — перевіряється і при додаванні учня в групу,
// і при створенні / зміні слоту розкладу.
@SpringBootTest
@ActiveProfiles("test")
class GangMembershipTest {

    @Autowired private GangService gangService;
    @Autowired private ScheduleService scheduleService;
    @Autowired private StudentService studentService;

    @Autowired private GangsStudentsRepository gangsStudentsRepository;
    @Autowired private AudienceRepository audienceRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private GangRepository gangRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Subject subject;
    private Audience audience3;
    private Teacher teacher3;
    private Gang english;   // Петренко; вівторок 10:00–11:00
    private Gang math;      // без учнів; вівторок 10:30–11:30 — перетинається з english
    private Gang art;       // Петренко; без слотів
    private Student petrenko;
    private Student ivanenko;

    @BeforeEach
    void setUp() {
        subject = subjectRepository.save(Subject.builder().name("Англійська").build());
        Audience audience1 = audienceRepository.save(Audience.builder().name("101").build());
        Audience audience2 = audienceRepository.save(Audience.builder().name("102").build());
        audience3 = audienceRepository.save(Audience.builder().name("103").build());
        Teacher teacher1 = teacher("Шевченко");
        Teacher teacher2 = teacher("Коваль");
        teacher3 = teacher("Бойко");

        petrenko = student("Петренко");
        ivanenko = student("Іваненко");

        english = gangRepository.save(Gang.builder().name("Англ-1").subject(subject).teacher(teacher1).build());
        math = gangRepository.save(Gang.builder().name("Мат-1").subject(subject).teacher(teacher2).build());
        art = gangRepository.save(Gang.builder().name("Арт-1").subject(subject).teacher(teacher3).build());
        gangsStudentsRepository.save(GangsStudents.builder().gang(english).student(petrenko).build());
        gangsStudentsRepository.save(GangsStudents.builder().gang(art).student(petrenko).build());

        slot(english, audience1, teacher1, "10:00", "11:00");
        slot(math, audience2, teacher2, "10:30", "11:30");
    }

    @AfterEach
    void cleanUp() {
        // тест без @Transactional — прибираємо за собою вручну (від залежних таблиць до основних)
        for (String table : List.of("schedule", "gangs_students", "gang", "student", "teacher", "subject", "audience")) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }

    // ---- додавання / прибирання учня ----

    @Test
    void shouldAddStudent_WhenStudentIsFree() {
        gangService.addStudent(math.getId(), ivanenko.getId());

        assertThat(gangsStudentsRepository.existsByGangIdAndStudentId(math.getId(), ivanenko.getId())).isTrue();
    }

    @Test
    void shouldRejectAdding_WhenNoStudentSelected() {
        assertThatThrownBy(() -> gangService.addStudent(math.getId(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Оберіть учня");
    }

    @Test
    void shouldRejectStudent_WhenAlreadyInGang() {
        assertThatThrownBy(() -> gangService.addStudent(english.getId(), petrenko.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("уже в цій групі");
    }

    @Test
    void shouldRejectStudent_WhenBusyInAnotherGangAtThatTime() {
        // Петренко в Англ-1 (вт 10:00–11:00), а Мат-1 займається вт 10:30–11:30
        assertThatThrownBy(() -> gangService.addStudent(math.getId(), petrenko.getId()))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("Петренко")
                .hasMessageContaining("Англ-1");

        assertThat(gangsStudentsRepository.existsByGangIdAndStudentId(math.getId(), petrenko.getId())).isFalse();
    }

    @Test
    void shouldRemoveStudent_ThenAllowArchiveAndAddingBack() {
        gangService.removeStudent(english.getId(), petrenko.getId());
        gangService.removeStudent(art.getId(), petrenko.getId());
        assertThat(gangsStudentsRepository.existsByGangIdAndStudentId(english.getId(), petrenko.getId())).isFalse();

        // ні в одній групі — тепер учня можна архівувати (раніше: "спершу приберіть з групи")
        studentService.archive(petrenko.getId());
        // учня в архіві в групу не додати
        assertThatThrownBy(() -> gangService.addStudent(english.getId(), petrenko.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("в архіві");

        // повернувся — його можна знову додати в ту саму групу (прибраний запис лишився в базі м'яко видаленим)
        studentService.restore(petrenko.getId());
        gangService.addStudent(english.getId(), petrenko.getId());
        assertThat(gangsStudentsRepository.existsByGangIdAndStudentId(english.getId(), petrenko.getId())).isTrue();
    }

    @Test
    void shouldRejectRemoving_WhenStudentNotInGang() {
        assertThatThrownBy(() -> gangService.removeStudent(math.getId(), ivanenko.getId()))
                .isInstanceOf(BusinessException.class);
    }

    // ---- розклад: спільний учень у двох групах (справжня база, без заглушок) ----

    @Test
    void shouldRejectSlot_WhenSharedStudentIsBusy() {
        // Арт-1 і Англ-1 мають спільного учня Петренка; кабінет і вчитель — інші
        assertThatThrownBy(() -> scheduleService.create(artSlot(WeekDay.ВІВТОРОК, "10:15", "10:45")))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("Петренко");
    }

    @Test
    void shouldRejectSlotUpdate_WhenItMovesOntoSharedStudentsLesson() {
        // середа — конфлікту немає
        int artSlotId = scheduleService.create(artSlot(WeekDay.СЕРЕДА, "10:00", "11:00")).getId();

        // переносимо на вівторок 10:00 — там у Петренка Англ-1
        assertThatThrownBy(() -> scheduleService.update(artSlotId, artSlot(WeekDay.ВІВТОРОК, "10:00", "11:00")))
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessageContaining("Петренко");
    }

    @Test
    void shouldAllowSlot_WhenSharedStudentWasRemovedFromGang() {
        gangService.removeStudent(art.getId(), petrenko.getId());

        // спільних учнів більше немає — слот на той самий час дозволено
        scheduleService.create(artSlot(WeekDay.ВІВТОРОК, "10:15", "10:45"));
        assertThat(scheduleRepository.findAllByGangId(art.getId())).hasSize(1);
    }

    private ScheduleDto artSlot(WeekDay day, String start, String end) {
        return ScheduleDto.builder()
                .subjectId(subject.getId()).gangId(art.getId())
                .teacherId(teacher3.getId()).audienceId(audience3.getId())
                .weekDay(day).startTime(LocalTime.parse(start)).endTime(LocalTime.parse(end))
                .build();
    }

    private void slot(Gang gang, Audience audience, Teacher teacher, String start, String end) {
        scheduleRepository.save(Schedule.builder()
                .gang(gang).subject(subject).audience(audience).teacher(teacher)
                .weekDay(WeekDay.ВІВТОРОК).startTime(LocalTime.parse(start)).endTime(LocalTime.parse(end))
                .build());
    }

    private Teacher teacher(String lastName) {
        return teacherRepository.save(Teacher.builder().firstName("Вчитель").lastName(lastName).build());
    }

    private Student student(String lastName) {
        return studentRepository.save(Student.builder()
                .firstName("Учень").lastName(lastName)
                .contactName("мама").phone("0670000000")
                .gender(Gender.ДІВЧИНА).studentStatus(StudentStatus.УЧЕНЬ)
                .balance(BigDecimal.ZERO)
                .build());
    }
}
