package com.krailo.smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStudentDto {

    private Integer id;
    private Integer lessonId;
    private LocalDate lessonDate;
    private String subjectName;
    private Integer paymentId;
    private LocalDate paymentDate;
    private Integer studentId;
    private String studentLastName;
    private String studentFirstName;
    boolean studentPresent;
    boolean lessonPayed;
    private int subjectFulPrice;
    private int discountPercentage;
    private double discountSum;
    private double debit;
    private double credit;
    private double balance;

}
