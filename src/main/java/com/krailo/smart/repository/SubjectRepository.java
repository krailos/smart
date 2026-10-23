package com.krailo.smart.repository;

import com.krailo.smart.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    // archived = false — активні (для списків і форм), true — архів
    List<Subject> findAllByArchived(boolean archived);

}
