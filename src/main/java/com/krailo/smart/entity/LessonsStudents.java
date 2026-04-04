package com.krailo.smart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = {"lesson", "student"})
@ToString(exclude =  {"lesson", "student"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lessons_students")
public class LessonsStudents {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
    @Column(name = "is_student_present")
    private boolean present;
    @Column(name = "is_lesson_payed")
    private boolean payed;
    
//    public void setLesson (Lesson lesson) {
//        this.lesson = lesson;
//        this.lesson.getLessonStudents().add(this);
//    }
//    
//    public void setStudent (Student student) {
//        this.student = student;
//        this.student.getStudentLessons().add(this);
//    }
    
    

}
