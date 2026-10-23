package com.krailo.smart.repository;

import com.krailo.smart.entity.*;
import com.krailo.smart.enumeration.WeekDay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LessonRepositoryTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 19);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LessonRepository lessonRepository;

    private Schedule schedule; // слот розкладу: понеділок 10:00–11:00

    @BeforeEach
    void setUp() {
        Audience audience = entityManager.persist(Audience.builder().name("101").build());
        Subject subject = entityManager.persist(Subject.builder().name("Англійська").build());
        Teacher teacher = entityManager.persist(Teacher.builder().firstName("Ірина").lastName("Шевченко").build());
        Gang gang = entityManager.persist(Gang.builder().name("Англ-1").subject(subject).teacher(teacher).build());
        schedule = entityManager.persist(Schedule.builder()
                .audience(audience).subject(subject).gang(gang).teacher(teacher)
                .weekDay(WeekDay.ПОНЕДІЛОК)
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                .build());

        // урок за цим розкладом уже проведено 19.10
        entityManager.persistAndFlush(lessonFrom(schedule, MONDAY));
    }

    @Test
    void shouldFindHeldLesson_WhenSameScheduleAndDate() {
        assertThat(lessonRepository.existsByScheduleIdAndDate(schedule.getId(), MONDAY)).isTrue();
    }

    @Test
    void shouldNotFindLesson_WhenOtherDate() {
        // наступний понеділок — новий урок за тим самим розкладом, це нормально
        assertThat(lessonRepository.existsByScheduleIdAndDate(schedule.getId(), MONDAY.plusDays(7))).isFalse();
    }

    @Test
    void shouldIgnoreLessonItself_WhenCheckingOnUpdate() {
        // при редагуванні урок не повинен "конфліктувати" сам із собою
        Lesson held = lessonRepository.findAll().get(0);
        assertThat(lessonRepository.existsByScheduleIdAndDateAndIdNot(schedule.getId(), MONDAY, held.getId())).isFalse();
        // а для будь-якого іншого уроку цей день зайнятий
        assertThat(lessonRepository.existsByScheduleIdAndDateAndIdNot(schedule.getId(), MONDAY, held.getId() + 1)).isTrue();
    }

    @Test
    void shouldRejectSecondLesson_WhenSameScheduleAndDate() {
        // база сама не дасть зберегти другий урок з того ж слоту на ту ж дату (обмеження UNIQUE)
        assertThatThrownBy(() -> entityManager.persistAndFlush(lessonFrom(schedule, MONDAY)))
                .isInstanceOf(Exception.class);
    }

    private Lesson lessonFrom(Schedule s, LocalDate date) {
        return Lesson.builder()
                .schedule(s)
                .subject(s.getSubject()).gang(s.getGang()).teacher(s.getTeacher()).audience(s.getAudience())
                .date(date)
                .startTime(s.getStartTime()).endTime(s.getEndTime())
                .build();
    }
}
