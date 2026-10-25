package com.krailo.smart.calendar;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.enumeration.WeekDay;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

// Будує тижневий календар із розкладу: межі сітки, позиції занять, "доріжки" для занять, що перетинаються.
// Лише розрахунок, без бази — тому легко перевіряється тестами (ScheduleCalendarTest).
@Component
public class ScheduleCalendar {

    static final int SLOT_MINUTES = 30;
    private static final LocalTime DEFAULT_START = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_END = LocalTime.of(23, 0);
    private static final LocalTime LAST_MINUTE = LocalTime.of(23, 59);
    private static final Map<WeekDay, String> LABELS = Map.of(
            WeekDay.ПОНЕДІЛОК, "Пн", WeekDay.ВІВТОРОК, "Вт", WeekDay.СЕРЕДА, "Ср", WeekDay.ЧЕТВЕР, "Чт",
            WeekDay.ПЯТНИЦЯ, "Пт", WeekDay.СУБОТА, "Сб", WeekDay.НЕДІЛЯ, "Нд");

    // Фільтри — null, якщо не вибрані. Заняття, що не відповідає вибраним фільтрам, позначається dimmed:
    // так видно, коли саме ЦЯ аудиторія / вчитель / група вільні, не втрачаючи загальної картини.
    public WeekCalendar build(List<ScheduleDto> schedules, Integer audienceId, Integer teacherId, Integer gangId) {
        // сітка 08:00–23:00, але розширюється, якщо якесь заняття за її межами
        LocalTime start = DEFAULT_START;
        LocalTime end = DEFAULT_END;
        for (ScheduleDto s : schedules) {
            if (s.getStartTime().isBefore(start)) {
                start = s.getStartTime().truncatedTo(ChronoUnit.HOURS);
            }
            if (s.getEndTime().isAfter(end)) {
                end = ceilToHour(s.getEndTime());
            }
        }

        List<LocalTime> rows = new ArrayList<>();
        for (LocalTime t = start; t.isBefore(end); t = t.plusMinutes(SLOT_MINUTES)) {
            rows.add(t);
            if (t.plusMinutes(SLOT_MINUTES).isBefore(t)) {
                break; // перейшли через північ
            }
        }

        Predicate<ScheduleDto> matches = s -> (audienceId == null || Objects.equals(s.getAudienceId(), audienceId))
                && (teacherId == null || Objects.equals(s.getTeacherId(), teacherId))
                && (gangId == null || Objects.equals(s.getGangId(), gangId));

        List<WeekCalendar.Day> days = new ArrayList<>();
        for (WeekDay weekDay : WeekDay.values()) {
            List<ScheduleDto> daySchedules = schedules.stream().filter(s -> s.getWeekDay() == weekDay).toList();
            List<WeekCalendar.Event> events = layout(daySchedules, start, matches);
            int maxLanes = events.stream().mapToInt(WeekCalendar.Event::getLanes).max().orElse(1);
            days.add(new WeekCalendar.Day(weekDay, LABELS.get(weekDay), events, maxLanes));
        }
        return new WeekCalendar(start, end, rows, days);
    }

    // Заняття одного дня -> події з позиціями. Ланцюжок занять, що перетинаються, — "група перетину":
    // усі заняття в ній ділять ширину колонки на однакову кількість доріжок.
    private List<WeekCalendar.Event> layout(List<ScheduleDto> daySchedules, LocalTime gridStart, Predicate<ScheduleDto> matches) {
        List<ScheduleDto> sorted = daySchedules.stream()
                .sorted(Comparator.comparing(ScheduleDto::getStartTime).thenComparing(ScheduleDto::getEndTime))
                .toList();
        List<WeekCalendar.Event> events = new ArrayList<>();
        List<ScheduleDto> cluster = new ArrayList<>();
        LocalTime clusterEnd = null;
        for (ScheduleDto s : sorted) {
            // починається не раніше, ніж закінчилась уся група — нова група
            if (clusterEnd != null && !s.getStartTime().isBefore(clusterEnd)) {
                events.addAll(placeCluster(cluster, gridStart, matches));
                cluster.clear();
                clusterEnd = null;
            }
            cluster.add(s);
            if (clusterEnd == null || s.getEndTime().isAfter(clusterEnd)) {
                clusterEnd = s.getEndTime();
            }
        }
        events.addAll(placeCluster(cluster, gridStart, matches));
        return events;
    }

    // у групі перетину кожне заняття стає на першу доріжку, яка вже звільнилась (або відкриває нову)
    private List<WeekCalendar.Event> placeCluster(List<ScheduleDto> cluster, LocalTime gridStart, Predicate<ScheduleDto> matches) {
        List<LocalTime> laneEnds = new ArrayList<>();
        int[] laneOf = new int[cluster.size()];
        for (int i = 0; i < cluster.size(); i++) {
            ScheduleDto s = cluster.get(i);
            int lane = 0;
            while (lane < laneEnds.size() && laneEnds.get(lane).isAfter(s.getStartTime())) {
                lane++;
            }
            if (lane == laneEnds.size()) {
                laneEnds.add(s.getEndTime());
            } else {
                laneEnds.set(lane, s.getEndTime());
            }
            laneOf[i] = lane;
        }
        List<WeekCalendar.Event> events = new ArrayList<>();
        for (int i = 0; i < cluster.size(); i++) {
            ScheduleDto s = cluster.get(i);
            events.add(new WeekCalendar.Event(s,
                    (int) Duration.between(gridStart, s.getStartTime()).toMinutes(),
                    (int) Duration.between(s.getStartTime(), s.getEndTime()).toMinutes(),
                    laneOf[i], laneEnds.size(), !matches.test(s)));
        }
        return events;
    }

    // 20:30 -> 21:00, 21:00 -> 21:00; після 23:00 — до кінця доби
    private LocalTime ceilToHour(LocalTime time) {
        LocalTime hour = time.truncatedTo(ChronoUnit.HOURS);
        if (hour.equals(time)) {
            return time;
        }
        return hour.getHour() == 23 ? LAST_MINUTE : hour.plusHours(1);
    }
}
