package com.krailo.smart.dto;

import lombok.*;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStudentDto {

    private Integer id;
    private LocalDate date;
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
    private int discountValue;
    private double debit;
    private double credit;
    
}
