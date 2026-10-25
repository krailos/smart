package com.krailo.smart.calendar;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.enumeration.WeekDay;
import lombok.Value;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

// Тижневий календар розкладу: що і де намалювати. Заповнює ScheduleCalendar.build.
// Позиції — у ХВИЛИНАХ від початку сітки; у пікселі їх переводить CSS (.week у app.css),
// тож масштаб сітки змінюється в одному місці.
@Value
public class WeekCalendar {

    LocalTime start;          // початок сітки (ціла година)
    LocalTime end;            // кінець сітки
    List<LocalTime> rows;     // початки клітинок по 30 хв: 08:00, 08:30, ...
    List<Day> days;           // 7 колонок: ПОНЕДІЛОК ... НЕДІЛЯ

    public int getTotalMinutes() {
        return (int) Duration.between(start, end).toMinutes();
    }

    @Value
    public static class Day {
        WeekDay weekDay;
        String label;         // "Пн"
        List<Event> events;
    }

    // Одне заняття: зверху startMinute, висота durationMinutes.
    // Заняття, що перетинаються, стоять поруч: lane — номер "доріжки", lanes — скільки доріжок у групі перетину.
    @Value
    public static class Event {
        ScheduleDto schedule;
        int startMinute;
        int durationMinutes;
        int lane;
        int lanes;
        boolean dimmed;       // не відповідає фільтру (аудиторія / вчитель / група) — показуємо блідим
    }
}
