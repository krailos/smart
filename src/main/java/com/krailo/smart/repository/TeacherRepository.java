package com.krailo.smart.repository;

import com.krailo.smart.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TeacherRepository extends JpaRepository<Teacher, Integer> {

    // archived = false — активні (для списків і форм), true — архів
    List<Teacher> findAllByArchived(boolean archived);

}
