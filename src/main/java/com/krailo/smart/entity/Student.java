package com.krailo.smart.entity;

import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"gangsStudents", "studentsDiscounts", "payments", "lessonsStudents", "balanceStudents"})
@ToString(exclude = {"gangsStudents", "studentsDiscounts", "payments", "lessonsStudents", "balanceStudents"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<GangsStudents> gangsStudents;
    private String firstName;
    private String secondName;
    private String lastName;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    private StudentStatus studentStatus;
    private LocalDate birthDate;
    private String description;
    // архів: запис зникає зі списків і випадаючих меню форм, але лишається в історії (уроки, оплати, розклад);
    // на відміну від @SoftDelete, Hibernate його НЕ ховає — тому уроки й оплати й далі бачать ім'я
    @Column(name = "is_deleted")
    private boolean archived;
    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    @Builder.Default
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<StudentsDiscounts> studentsDiscounts = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<LessonsStudents> lessonsStudents = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "student")
    private List<TransactionStudent> balanceStudents = new ArrayList<>();


//    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
//    private List<Lesson> lessons;


}
