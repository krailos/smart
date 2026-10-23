package com.krailo.smart.repository;

import com.krailo.smart.entity.GangsStudents;
import org.springframework.data.jpa.repository.JpaRepository;


public interface GangsStudentsRepository extends JpaRepository<GangsStudents, Integer> {

    // перевірка перед архівуванням учня: чи він ще в якійсь активній групі
    boolean existsByStudentIdAndGangArchivedFalse(Integer studentId);
}
