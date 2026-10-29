package com.krailo.smart.repository;

import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Student;
import com.krailo.smart.entity.TransactionStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionStudentRepository extends JpaRepository<TransactionStudent, Integer> {

    List<TransactionStudent> findAllByLesson (Lesson lesson);

    // увесь журнал учня — для звіту (виписки) за період
    List<TransactionStudent> findAllByStudent (Student student);

    // дата першої операції учня (урок або оплата) — початковий баланс не може бути пізнішим за неї.
    // Самі початкові баланси (і їхні сторно) не рахуються: після сторно залишок вносять заново
    @Query("select min(coalesce(l.date, p.date)) from TransactionStudent t left join t.lesson l left join t.payment p"
            + " where t.student.id = :studentId and (p is null or p.openingBalance = false)")
    Optional<LocalDate> findFirstOperationDate(@Param("studentId") Integer studentId);

}
