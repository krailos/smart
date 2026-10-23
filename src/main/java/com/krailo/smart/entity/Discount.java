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
    // архів: запис зникає зі списків і випадаючих меню форм, але лишається в історії (уроки, оплати, розклад);
    // на відміну від @SoftDelete, Hibernate його НЕ ховає — тому уроки й оплати й далі бачать ім'я
    @Column(name = "is_deleted")
    private boolean archived;
    @OneToMany(mappedBy = "discount")
    private List<StudentsDiscounts> studentsDiscounts;

}
