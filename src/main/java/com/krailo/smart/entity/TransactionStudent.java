package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transaction_student")
public class TransactionStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
    @Builder.Default
    @Column(name = "subject_full_price", precision = 10, scale = 2)
    private BigDecimal subjectFulPrice = BigDecimal.ZERO;
    @Column(name = "discount_percentage")
    private int discountPercentage;
    @Column(name = "is_student_present")
    private boolean studentPresent;
    @Column(name = "is_lesson_payed")
    private boolean lessonPayed;
    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal discountSum = BigDecimal.ZERO;
    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal debit = BigDecimal.ZERO;
    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal credit = BigDecimal.ZERO;
    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

}
