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
            """)
    List<Lesson> findAllByStudentAndBetweenDates (Student student, LocalDate start, LocalDate end);
      

}
