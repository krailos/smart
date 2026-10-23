package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Data
@EqualsAndHashCode(exclude = {"gangs", "schedules", "lessons"})
@ToString(exclude = {"gangs", "schedules", "lessons"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "second_name")
    private String secondName;
    @Column(name = "last_name")
    private String lastName;
    private String phone;
    private String email;
    private String address;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    private String description;
    // архів: запис зникає зі списків і випадаючих меню форм, але лишається в історії (уроки, оплати, розклад);
    // на відміну від @SoftDelete, Hibernate його НЕ ховає — тому уроки й оплати й далі бачать ім'я
    @Column(name = "is_deleted")
    private boolean archived;
    @OneToMany (mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<Gang> gangs;
    @OneToMany (mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<Schedule> schedules;
    @OneToMany (mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<Lesson> lessons;

  

}
