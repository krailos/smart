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

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ScheduleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private Schedule existing; // заняття, яке вже є: вівторок 10:00–11:00

    @BeforeEach
    void setUp() {
        Audience audience = entityManager.persist(Audience.builder().name("101").build());
        Subject subject = entityManager.persist(Subject.builder().name("Англійська").build());
        Teacher teacher = entityManager.persist(Teacher.builder().firstName("Ірина").lastName("Шевченко").build());
        Gang gang = entityManager.persist(Gang.builder().name("Англ-1").subject(subject).teacher(teacher).build());

        existing = entityManager.persistAndFlush(Schedule.builder()
                .audience(audience).subject(subject).gang(gang).teacher(teacher)
                .weekDay(WeekDay.ВІВТОРОК)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build());
    }

    @Test
    void shouldFindSchedule_WhenTimePartiallyOverlaps() {
        // починається під час існуючого заняття
        assertThat(find(WeekDay.ВІВТОРОК, "10:30", "11:30")).containsExactly(existing);
        // закінчується під час існуючого заняття
        assertThat(find(WeekDay.ВІВТОРОК, "09:30", "10:30")).containsExactly(existing);
        // повністю всередині
        assertThat(find(WeekDay.ВІВТОРОК, "10:15", "10:45")).containsExactly(existing);
        // повністю накриває
        assertThat(find(WeekDay.ВІВТОРОК, "09:00", "12:00")).containsExactly(existing);
    }

    @Test
    void shouldNotFindSchedule_WhenLessonsAreBackToBack() {
        // заняття "впритул" — не конфлікт
        assertThat(find(WeekDay.ВІВТОРОК, "11:00", "12:00")).isEmpty();
        assertThat(find(WeekDay.ВІВТОРОК, "09:00", "10:00")).isEmpty();
    }

    @Test
    void shouldNotFindSchedule_WhenOtherWeekDay() {
        assertThat(find(WeekDay.СЕРЕДА, "10:00", "11:00")).isEmpty();
    }

    @Test
    void shouldNotFindItself_WhenExcludeIdIsGiven() {
        // під час редагування розклад не повинен конфліктувати сам із собою
        assertThat(scheduleRepository.findOverlappingSchedules(
                WeekDay.ВІВТОРОК, LocalTime.of(10, 0), LocalTime.of(11, 0), existing.getId())).isEmpty();
    }

    private List<Schedule> find(WeekDay day, String start, String end) {
        return scheduleRepository.findOverlappingSchedules(day, LocalTime.parse(start), LocalTime.parse(end), 0);
    }
}
