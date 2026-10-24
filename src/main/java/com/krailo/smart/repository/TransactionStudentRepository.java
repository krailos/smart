package com.krailo.smart.repository;

import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Student;
import com.krailo.smart.entity.TransactionStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionStudentRepository extends JpaRepository<TransactionStudent, Integer> {

    List<TransactionStudent> findAllByLesson (Lesson lesson);

    // увесь журнал учня — для звіту (виписки) за період
    List<TransactionStudent> findAllByStudent (Student student);

}
