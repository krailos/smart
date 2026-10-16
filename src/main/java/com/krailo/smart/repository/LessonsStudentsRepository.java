package com.krailo.smart.repository;

import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.LessonsStudents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface LessonsStudentsRepository extends JpaRepository<LessonsStudents, Integer> {

    List<LessonsStudents> findAllByLesson (Lesson lesson);
}
