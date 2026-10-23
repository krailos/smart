package com.krailo.smart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"subject","gang", "audience", "lessonsStudents", "schedule" })
@ToString(exclude = {"subject","gang", "audience", "lessonsStudents", "schedule" })
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
// один слот розкладу = не більше одного уроку на дату (те саме обмеження є в міграції V17)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_id", "lesson_date"}))
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // з якого слоту розкладу проведено урок
    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
    @ManyToOne
    @JoinColumn(name = "gang_id")
    private Gang gang;
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;
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
    @Builder.Default
    private List<LessonsStudents> lessonsStudents = new ArrayList<>();

}
