package com.krailo.smart.repository;

import com.krailo.smart.entity.Payment;
import com.krailo.smart.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;



public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    List<Payment> findAllByStudent(Student student);

}
