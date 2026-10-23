package com.krailo.smart.repository;

import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;




public interface GangRepository extends JpaRepository<Gang, Integer> {
    List<Gang> findAllByTeacher(Teacher teacher);

    // archived = false — активні (для списків і форм), true — архів
    List<Gang> findAllByArchived(boolean archived);

    // перевірки перед архівуванням вчителя / курсу: чи є в них активні групи
    boolean existsByTeacherIdAndArchivedFalse(Integer teacherId);

    boolean existsBySubjectIdAndArchivedFalse(Integer subjectId);
}
