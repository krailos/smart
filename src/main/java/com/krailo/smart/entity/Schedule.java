package com.krailo.smart.entity;

import com.krailo.smart.enumeration.WeekDay;
import jakarta.persistence.*;
import org.hibernate.annotations.SoftDelete;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// видалення = м'яке (is_deleted = true): Hibernate сам ховає такі рядки з усіх запитів;
// це "зв'язок без історії" — після видалення він справді не потрібен ніде
@SoftDelete(columnName = "is_deleted")
@Entity
public class Schedule {

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "audience_id")
    private Audience audience;
    @ManyToOne
    @JoinColumn(name = "gang_id")
    private Gang gang;
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
    @Enumerated(EnumType.STRING)
    private WeekDay weekDay;
    private LocalTime startTime;
    private LocalTime endTime;

    
}
