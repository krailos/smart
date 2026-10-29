package com.krailo.smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private boolean openingBalance; // оплата — це початковий баланс учня (або його сторно)
    private Integer studentId;
    private String studentLastName;
    private String studentFirstName;
    boolean studentPresent;
    boolean lessonPayed;
    private BigDecimal subjectFulPrice;
    private int discountPercentage;
    private BigDecimal discountSum;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal balance;

}
