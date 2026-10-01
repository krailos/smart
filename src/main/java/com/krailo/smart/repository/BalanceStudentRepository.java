package com.krailo.smart.repository;

import com.krailo.smart.entity.BalanceStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BalanceStudentRepository extends JpaRepository<BalanceStudent, Integer> {

    @Query(value = "SELECT * FROM balance_student " +
            "WHERE student_id = :studentId " +
            "AND transaction_date <= :lessonDate  " +
            "ORDER BY transaction_date DESC, id DESC " +
            "LIMIT 2",
            nativeQuery = true)
    List<BalanceStudent> findByDate (@Param("studentId")Integer studentId, @Param("lessonDate")LocalDate lessonDate);

}
