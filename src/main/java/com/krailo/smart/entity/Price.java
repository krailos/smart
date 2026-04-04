package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(exclude = "subject")
@ToString(exclude = "subject")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    @Column(name = "price_value")
    private int value;
    @Column(name = "price_date")
    private LocalDate date;
    @ManyToOne()
    @JoinColumn(name = "subject_id")
    private Subject subject;
    
}
