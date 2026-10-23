package com.krailo.smart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SoftDelete;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = {"gang", "student"})
@ToString(exclude =  {"gang", "student"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
// видалення = м'яке (is_deleted = true): Hibernate сам ховає такі рядки з усіх запитів;
// це "зв'язок без історії" — після видалення він справді не потрібен ніде
@SoftDelete(columnName = "is_deleted")
@Entity
@Table(name = "gangs_students")
public class GangsStudents {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "gang_id")
    private Gang gang;
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

}
