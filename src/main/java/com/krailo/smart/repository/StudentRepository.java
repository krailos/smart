package com.krailo.smart.repository;

import com.krailo.smart.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;



public interface StudentRepository extends JpaRepository<Student, Integer> {

}
