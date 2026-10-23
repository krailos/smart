package com.krailo.smart.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SoftDelete;
import lombok.*;

import java.time.LocalDate;


@Data
@EqualsAndHashCode(exclude = {"discount", "student", "subject"})
@ToString(exclude = {"discount", "student", "subject"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
// видалення = м'яке (is_deleted = true): Hibernate сам ховає такі рядки з усіх запитів;
// це "зв'язок без історії" — після видалення він справді не потрібен ніде
@SoftDelete(columnName = "is_deleted")
@Entity
@Table(name = "students_discounts")
public class StudentsDiscounts {
      
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
    @Column(name = "students_discounts_date")
    private LocalDate date;


}
