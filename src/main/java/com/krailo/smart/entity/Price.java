package com.krailo.smart.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SoftDelete;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(exclude = "subject")
@ToString(exclude = "subject")
@NoArgsConstructor
@AllArgsConstructor
@Builder
// видалення = м'яке (is_deleted = true): Hibernate сам ховає такі рядки з усіх запитів;
// це "зв'язок без історії" — після видалення він справді не потрібен ніде
@SoftDelete(columnName = "is_deleted")
@Entity
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    @Column(name = "price_value", precision = 10, scale = 2)
    private BigDecimal value;
    @Column(name = "price_date")
    private LocalDate date;
    @ManyToOne()
    @JoinColumn(name = "subject_id")
    private Subject subject;
    
}
