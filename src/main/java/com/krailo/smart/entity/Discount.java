package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Data
@EqualsAndHashCode(exclude = {"studentsDiscounts"})
@ToString(exclude ={"studentsDiscounts"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Discount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    @Column(name = "discount_value")
    private int value;
    @Column(name = "discount_date")
    private LocalDate date;
    @OneToMany(mappedBy = "discount")
    private List<StudentsDiscounts> studentsDiscounts;

}
