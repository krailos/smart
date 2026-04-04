package com.krailo.smart.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.transaction.TransactionScoped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = {"subject","gang", "audience", "lessonsStudents" })
@ToString(exclude = {"subject","gang", "audience", "lessonsStudents" })
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Lesson {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
    @ManyToOne
    @JoinColumn(name = "gang_id")
    private Gang gang;
    @ManyToOne
    @JoinColumn(name = "audience_id")
    private Audience audience;
    @Column(name = "lesson_date")
    private LocalDate date;
    @Column(name = "lesson_start")
    private LocalTime startTime;
    @Column(name = "lesson_end")
    private LocalTime endTime;
    @OneToMany(mappedBy = "lesson")
    private List<LessonsStudents> lessonsStudents;
//    @ManyToMany (fetch = FetchType.LAZY)
//    @JoinTable(name = "lesson_and_student", 
//    joinColumns =  @JoinColumn(name = "lesson_id"),
//    inverseJoinColumns = @JoinColumn(name = "student_id"))
//    @Transient
//    private List<Student> students;

}
