package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

}
