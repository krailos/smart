package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Data
@EqualsAndHashCode(exclude = {"student", "stornoOf"})
@ToString(exclude ={"student", "stornoOf"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Payment {
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
    @Column(name = "payment_value")
    private int value;
    @Column(name = "payment_date")
    private LocalDate date;
    private String description;
    // якщо це сторно — оплата, яку воно "гасить" (сума тоді від'ємна); для звичайної оплати null
    @ManyToOne
    @JoinColumn(name = "storno_of_id")
    private Payment stornoOf;


}
