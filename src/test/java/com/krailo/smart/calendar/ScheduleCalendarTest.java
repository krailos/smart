package com.krailo.smart.calendar;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.enumeration.WeekDay;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Розрахунок тижневого календаря — без бази і без Spring: тільки позиції й "доріжки".
class ScheduleCalendarTest {

    private final ScheduleCalendar calendar = new ScheduleCalendar();

    @Test
    void shouldBuildDefaultGrid_WhenScheduleIsEmpty() {
        WeekCalendar week = calendar.build(List.of(), null, null, null);

        // 08:00–21:00 по 30 хв = 26 клітинок, 7 днів Пн…Нд
        assertThat(week.getStart()).isEqualTo(LocalTime.of(8, 0));
        assertThat(week.getEnd()).isEqualTo(LocalTime.of(21, 0));
        assertThat(week.getTotalMinutes()).isEqualTo(13 * 60);
        assertThat(week.getRows()).hasSize(26).first().isEqualTo(LocalTime.of(8, 0));
        assertThat(week.getDays()).extracting(WeekCalendar.Day::getLabel)
                .containsExactly("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд");
    }

    @Test
    void shouldExtendGrid_WhenLessonIsOutsideDefaultHours() {
        WeekCalendar week = calendar.build(List.of(
                slot(1, WeekDay.ПОНЕДІЛОК, "07:15", "08:00", 1),
                slot(2, WeekDay.ВІВТОРОК, "20:30", "21:30", 1)), null, null, null);

        assertThat(week.getStart()).isEqualTo(LocalTime.of(7, 0));
        assertThat(week.getEnd()).isEqualTo(LocalTime.of(22, 0));
    }

    @Test
    void shouldPlaceLessonByMinutesFromGridStart() {
        WeekCalendar week = calendar.build(List.of(slot(1, WeekDay.СЕРЕДА, "12:15", "13:00", 1)), null, null, null);

        WeekCalendar.Event ev = day(week, WeekDay.СЕРЕДА).getEvents().get(0);
        assertThat(ev.getStartMinute()).isEqualTo(4 * 60 + 15);   // від 08:00
        assertThat(ev.getDurationMinutes()).isEqualTo(45);
        assertThat(ev.getLanes()).isEqualTo(1);
        assertThat(day(week, WeekDay.ПОНЕДІЛОК).getEvents()).isEmpty();
    }

    @Test
    void shouldPutOverlappingLessonsSideBySide() {
        WeekCalendar week = calendar.build(List.of(
                slot(1, WeekDay.ВІВТОРОК, "10:00", "11:00", 1),
                slot(2, WeekDay.ВІВТОРОК, "10:30", "11:30", 2),
                slot(3, WeekDay.ВІВТОРОК, "11:30", "12:00", 3)), null, null, null);   // впритул — окремо

        List<WeekCalendar.Event> events = day(week, WeekDay.ВІВТОРОК).getEvents();
        assertThat(events).extracting(e -> e.getSchedule().getId(), WeekCalendar.Event::getLane, WeekCalendar.Event::getLanes)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1, 0, 2),
                        org.assertj.core.groups.Tuple.tuple(2, 1, 2),
                        org.assertj.core.groups.Tuple.tuple(3, 0, 1));
    }

    @Test
    void shouldReuseFreedLane() {
        // 10–11 і 10–12 — на різних доріжках; 11–12 стає на доріжку заняття 10–11, яка звільнилась о 11:00 —
        // разом 2 доріжки, а не 3
        WeekCalendar week = calendar.build(List.of(
                slot(1, WeekDay.ЧЕТВЕР, "10:00", "12:00", 1),
                slot(2, WeekDay.ЧЕТВЕР, "10:00", "11:00", 2),
                slot(3, WeekDay.ЧЕТВЕР, "11:00", "12:00", 3)), null, null, null);

        List<WeekCalendar.Event> events = day(week, WeekDay.ЧЕТВЕР).getEvents();
        assertThat(events).extracting(WeekCalendar.Event::getLanes).containsOnly(2);
        assertThat(lane(events, 3)).isEqualTo(lane(events, 2)).isNotEqualTo(lane(events, 1));
    }

    private int lane(List<WeekCalendar.Event> events, int scheduleId) {
        return events.stream().filter(e -> e.getSchedule().getId() == scheduleId).findFirst().orElseThrow().getLane();
    }

    @Test
    void shouldDimLessonsNotMatchingFilter() {
        List<ScheduleDto> schedules = List.of(
                slot(1, WeekDay.ПЯТНИЦЯ, "10:00", "11:00", 1),    // аудиторія 1
                slot(2, WeekDay.ПЯТНИЦЯ, "12:00", "13:00", 2));   // аудиторія 2

        assertThat(day(calendar.build(schedules, 1, null, null), WeekDay.ПЯТНИЦЯ).getEvents())
                .extracting(e -> e.getSchedule().getId(), WeekCalendar.Event::isDimmed)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(1, false), org.assertj.core.groups.Tuple.tuple(2, true));
        // без фільтра — нічого не бліде
        assertThat(day(calendar.build(schedules, null, null, null), WeekDay.ПЯТНИЦЯ).getEvents())
                .extracting(WeekCalendar.Event::isDimmed).containsOnly(false);
    }

    private WeekCalendar.Day day(WeekCalendar week, WeekDay weekDay) {
        return week.getDays().stream().filter(d -> d.getWeekDay() == weekDay).findFirst().orElseThrow();
    }

    // id слоту = id групи; аудиторія — окремо (для фільтра)
    private ScheduleDto slot(int id, WeekDay day, String start, String end, int audienceId) {
        return ScheduleDto.builder().id(id).gangId(id).audienceId(audienceId).teacherId(id)
                .weekDay(day).startTime(LocalTime.parse(start)).endTime(LocalTime.parse(end)).build();
    }
}
