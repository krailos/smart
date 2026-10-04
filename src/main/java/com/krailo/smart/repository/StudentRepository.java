package com.krailo.smart.repository;

import com.krailo.smart.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface StudentRepository extends JpaRepository<Student, Integer> {
    @Query("SELECT s FROM Student s WHERE LOWER(s.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);
}
