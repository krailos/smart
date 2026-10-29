package com.krailo.smart.repository;

import com.krailo.smart.entity.Payment;
import com.krailo.smart.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;



public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    List<Payment> findAllByStudent(Student student);

    // чи вже є сторно для цієї оплати
    boolean existsByStornoOfId(Integer paymentId);

    // id усіх оплат, які вже сторновано — для списку оплат одним запитом, а не existsByStornoOfId на кожну
    @Query("select p.stornoOf.id from Payment p where p.stornoOf is not null")
    Set<Integer> findStornedIds();

    // чи є в учня діючий початковий баланс: не сторно і ще не сторнований (правило "один раз на учня")
    @Query("select count(p) > 0 from Payment p where p.student.id = :studentId and p.openingBalance = true"
            + " and p.stornoOf is null and not exists (select s from Payment s where s.stornoOf = p)")
    boolean existsActiveOpeningBalance(@Param("studentId") Integer studentId);

}
