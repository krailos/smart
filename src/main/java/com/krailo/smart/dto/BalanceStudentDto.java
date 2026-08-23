package com.krailo.smart.dto;

import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Payment;
import com.krailo.smart.entity.Student;
import lombok.Value;

import java.time.LocalDate;

@Value
public class BalanceStudentDto {


    private Integer id;
    private LocalDate date;
    private Lesson lesson;
    private Integer lessonId;
    private Payment payment;
    private Integer paymentId;
    private Student student;
    private Integer studentId;
    private int debit;
    private int credit;
    private int balance;
    
}
