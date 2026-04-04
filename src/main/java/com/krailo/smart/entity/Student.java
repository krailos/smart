package com.krailo.smart.entity;

import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"studentGangs", "studentsDiscounts", "payments", "lessonsStudents"})
@ToString(exclude =  {"studentGangs", "studentsDiscounts", "payments", "lessonsStudents"})
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
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<StudentsDiscounts> studentsDiscounts;
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<Payment> payments;
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<LessonsStudents> lessonsStudents;
    
    
//    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
//    private List<Lesson> lessons;
    


}
