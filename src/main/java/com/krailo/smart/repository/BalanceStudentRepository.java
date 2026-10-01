package com.krailo.smart.repository;

import com.krailo.smart.entity.BalanceStudent;
import com.krailo.smart.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BalanceStudentRepository extends JpaRepository<BalanceStudent, Integer> {

    @Query("""
            SELECT bs from BalanceStudent bs
            WHERE bs.student = :student
            AND bs.date <= :lessonDate ORDER BY bs.date, bs.id DESC
            """)
    List<BalanceStudent> findByDate (Student student, LocalDate lessonDate);

}
