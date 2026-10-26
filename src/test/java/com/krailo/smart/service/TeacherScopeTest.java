package com.krailo.smart.service;

import com.krailo.smart.config.CurrentUser;
import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.dto.TeacherGangDto;
import com.krailo.smart.entity.*;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.Role;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.enumeration.WeekDay;
import com.krailo.smart.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Вчитель бачить тільки "своє": заняття, які він веде, їхні уроки (і ті, що провів на заміні) та учнів
@SpringBootTest
@ActiveProfiles("test")
class TeacherScopeTest {

    @Autowired private ScheduleService scheduleService;
    @Autowired private LessonService lessonService;
    @Autowired private StudentService studentService;
    @Autowired private CurrentUser currentUser;
    @Autowired private UserRepository userRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private AudienceRepository audienceRepository;
    @Autowired private GangRepository gangRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private GangsStudentsRepository gangsStudentsRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Teacher teacherA;
    private Teacher teacherB;
    private Schedule slotA;
    private Schedule slotB;

    @BeforeEach
    void setUp() {
        Subject subject = subjectRepository.save(Subject.builder().name("Англійська").build());
        Audience audience = audienceRepository.save(Audience.builder().name("101").build());
        teacherA = teacherRepository.save(Teacher.builder().firstName("Анна").lastName("Коваль").build());
        teacherB = teacherRepository.save(Teacher.builder().firstName("Борис").lastName("Мельник").build());
        Gang gangA = gangRepository.save(Gang.builder().name("Англ-А").subject(subject).teacher(teacherA).build());
        Gang gangB = gangRepository.save(Gang.builder().name("Англ-Б").subject(subject).teacher(teacherB).build());

        // у групі А — активний учень і учень в архіві; у групі Б — свій учень
        gangsStudentsRepository.save(GangsStudents.builder().gang(gangA).student(student("Петренко", false)).build());
        gangsStudentsRepository.save(GangsStudents.builder().gang(gangA).student(student("Архівний", true)).build());
        gangsStudentsRepository.save(GangsStudents.builder().gang(gangB).student(student("Сидоренко", false)).build());

        slotA = scheduleRepository.save(slot(gangA, subject, audience, teacherA, WeekDay.ПОНЕДІЛОК));
        slotB = scheduleRepository.save(slot(gangB, subject, audience, teacherB, WeekDay.ВІВТОРОК));

        lesson(slotA, teacherA);   // А провела своє заняття
        lesson(slotB, teacherA);   // А замінила Б на його занятті
        lesson(slotB, teacherB);   // Б провів своє заняття

        userRepository.save(User.builder().name("anna").password("{noop}x").role(Role.TEACHER).teacher(teacherA).build());
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        for (String table : List.of("lesson", "schedule", "gangs_students", "gang", "student", "users",
                "teacher", "subject", "audience")) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }

    @Test
    void shouldShowTeacherOnlyOwnSchedule() {
        assertThat(scheduleService.findAllByTeacher(teacherA.getId())).extracting(ScheduleDto::getId)
                .containsExactly(slotA.getId());
        // логін вчителя без картки — нічого
        assertThat(scheduleService.findAllByTeacher(null)).isEmpty();
    }

    @Test
    void shouldShowTeacherOwnLessons_IncludingSubstitutionBothWays() {
        // А: своє заняття + заміна в Б
        assertThat(lessonService.findAllOfTeacher(teacherA.getId())).extracting(LessonDto::getTeacherId)
                .containsExactlyInAnyOrder(teacherA.getId(), teacherA.getId());
        // Б: своє заняття, проведене ним, і своє заняття, проведене заміною (А)
        assertThat(lessonService.findAllOfTeacher(teacherB.getId())).extracting(LessonDto::getTeacherId)
                .containsExactlyInAnyOrder(teacherA.getId(), teacherB.getId());
        assertThat(lessonService.findAllOfTeacher(null)).isEmpty();
    }

    @Test
    void shouldShowTeacherOnlyActiveStudentsOfOwnGangs() {
        List<TeacherGangDto> gangs = studentService.findGangsOfTeacher(teacherA.getId());

        assertThat(gangs).extracting(TeacherGangDto::getGangName).containsExactly("Англ-А");
        assertThat(gangs.get(0).getStudents()).extracting(TeacherGangDto.Student::getLastName)
                .containsExactly("Петренко");   // архівного немає, учня групи Б — теж
    }

    @Test
    void shouldLetTeacherOpenOnlyOwnSlot_AdminAny() {
        // увійшла вчителька А
        login("anna", Role.TEACHER);
        assertThat(currentUser.isAdmin()).isFalse();
        assertThat(currentUser.teacherId()).isEqualTo(teacherA.getId());
        currentUser.requireOwnSchedule(teacherA.getId());   // своє — можна
        assertThatThrownBy(() -> currentUser.requireOwnSchedule(teacherB.getId()))
                .isInstanceOf(AccessDeniedException.class);

        // адміну — будь-яке заняття
        login("boss", Role.ADMIN);
        assertThat(currentUser.isAdmin()).isTrue();
        currentUser.requireOwnSchedule(teacherB.getId());
    }

    // те, що робить Spring після входу: кладе користувача з його роллю в SecurityContextHolder
    private void login(String name, Role role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(name, null, List.of(role)));
    }

    private Student student(String lastName, boolean archived) {
        return studentRepository.save(Student.builder()
                .firstName("Учень").lastName(lastName)
                .contactName("мама").phone("0670000000")
                .gender(Gender.ДІВЧИНА).studentStatus(StudentStatus.УЧЕНЬ)
                .balance(BigDecimal.ZERO).archived(archived)
                .build());
    }

    private Schedule slot(Gang gang, Subject subject, Audience audience, Teacher teacher, WeekDay day) {
        return Schedule.builder().gang(gang).subject(subject).audience(audience).teacher(teacher)
                .weekDay(day).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0)).build();
    }

    private void lesson(Schedule slot, Teacher conductedBy) {
        lessonRepository.save(Lesson.builder()
                .schedule(slot).subject(slot.getSubject()).gang(slot.getGang()).audience(slot.getAudience())
                .teacher(conductedBy)
                .date(LocalDate.of(2026, 10, 26).plusDays(lessonRepository.count()))   // різні дати
                .startTime(slot.getStartTime()).endTime(slot.getEndTime())
                .build());
    }
}
