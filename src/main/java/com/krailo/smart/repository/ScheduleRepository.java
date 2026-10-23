package com.krailo.smart.repository;

import com.krailo.smart.entity.Schedule;
import com.krailo.smart.enumeration.WeekDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;


public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    // заняття того ж дня, що перетинаються за часом (включно з частковим перетином);
    // excludeId — id розкладу, який редагуємо, щоб він не конфліктував сам із собою (для нового = 0)
    @Query("""
            SELECT s FROM Schedule s
            WHERE s.weekDay = :weekDay
              AND s.startTime < :newEndTime AND s.endTime > :newStartTime
              AND s.id <> :excludeId
            """)
    List<Schedule> findOverlappingSchedules(
            @Param("weekDay") WeekDay weekDay,
            @Param("newStartTime") LocalTime newStartTime,
            @Param("newEndTime") LocalTime newEndTime,
            @Param("excludeId") int excludeId
    );


    // перевірки перед архівуванням: чи є запис у розкладі (м'яко видалені слоти Hibernate автоматично пропускає — @SoftDelete)
    boolean existsByTeacherId(Integer teacherId);

    boolean existsBySubjectId(Integer subjectId);

    boolean existsByAudienceId(Integer audienceId);

    boolean existsByGangId(Integer gangId);

}
