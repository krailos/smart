package com.krailo.smart.repository;

import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;



public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    
    @Query("""
            SELECT l from Lesson l 
            JOIN FETCH l.lessonsStudents ls
            WHERE ls.student = :student
            AND l.date BETWEEN :start AND :end
            AND l.storned = false
            """)
    List<Lesson> findAllByStudentAndBetweenDates (Student student, LocalDate start, LocalDate end);

    // чи вже проведено урок з цього слоту розкладу на цю дату
    boolean existsByScheduleIdAndDate(Integer scheduleId, LocalDate date);

    // те саме, але без уроку, який редагуємо (id)
    boolean existsByScheduleIdAndDateAndIdNot(Integer scheduleId, LocalDate date, Integer id);


}
