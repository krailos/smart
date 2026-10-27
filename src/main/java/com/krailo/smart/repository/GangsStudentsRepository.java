package com.krailo.smart.repository;

import com.krailo.smart.entity.GangsStudents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface GangsStudentsRepository extends JpaRepository<GangsStudents, Integer> {

    // перевірка перед архівуванням учня: чи він ще в якійсь активній групі
    boolean existsByStudentIdAndGangArchivedFalse(Integer studentId);

    // учень зараз у групі? (прибраних — м'яко видалених — Hibernate не бачить, @SoftDelete)
    boolean existsByGangIdAndStudentId(Integer gangId, Integer studentId);

    Optional<GangsStudents> findByGangIdAndStudentId(Integer gangId, Integer studentId);

    // групи учня (через зв'язок «учень у групі»)
    List<GangsStudents> findAllByStudentId(Integer studentId);
}
