package com.krailo.smart.repository;

import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.TransactionStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionStudentRepository extends JpaRepository<TransactionStudent, Integer> {

    @Query(value = "SELECT * FROM public.transaction_student " +
            "WHERE student_id = :studentId " +
            "AND transaction_date <= :lessonDate  " +
            "ORDER BY transaction_date DESC, id DESC " +
            "LIMIT 2",
            nativeQuery = true)
    List<TransactionStudent> findByAllByStudentAndDate (@Param("studentId")Integer studentId, @Param("lessonDate")LocalDate lessonDate);

    List<TransactionStudent> findAllByLesson (Lesson lesson);

}
