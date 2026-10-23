package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"prices", "gangs"})
@ToString(exclude = {"prices", "gangs"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    // архів: запис зникає зі списків і випадаючих меню форм, але лишається в історії (уроки, оплати, розклад);
    // на відміну від @SoftDelete, Hibernate його НЕ ховає — тому уроки й оплати й далі бачать ім'я
    @Column(name = "is_deleted")
    private boolean archived;
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private List<Price> prices;
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private List<Gang> gangs;
    
}
