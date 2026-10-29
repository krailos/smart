package com.krailo.smart.repository;

import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;



public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    
    @Query("""
            SELECT l from Lesson l 
            JOIN FETCH l.lessonsStudents ls
            WHERE ls.student = :student
            AND l.date BETWEEN :start AND :end
            AND l.storned = false
            """)
    List<Lesson> findAllByStudentAndBetweenDates (Student student, LocalDate start, LocalDate end);

    // чи вже проведено урок з цього слоту розкладу на цю дату
    boolean existsByScheduleIdAndDate(Integer scheduleId, LocalDate date);

    // те саме, але без уроку, який редагуємо (id)
    boolean existsByScheduleIdAndDateAndIdNot(Integer scheduleId, LocalDate date, Integer id);

    // уроки вчителя: які він провів сам + з його занять, які провела заміна.
    // LEFT JOIN — бо у сторнованого уроку зв'язку з розкладом уже немає, а він теж має бути в списку
    @Query("""
            SELECT l FROM Lesson l
            LEFT JOIN l.schedule s
            WHERE l.teacher.id = :teacherId OR s.teacher.id = :teacherId
            """)
    List<Lesson> findAllOfTeacher(Integer teacherId);

    // те саме посторінково — для списку /lessons
    @Query("""
            SELECT l FROM Lesson l
            LEFT JOIN l.schedule s
            WHERE l.teacher.id = :teacherId OR s.teacher.id = :teacherId
            """)
    Page<Lesson> findAllOfTeacher(Integer teacherId, Pageable pageable);

    // звіт «Уроки вчителів»: проведені (не сторновані) уроки за період; teacherId null — усіх вчителів.
    // Вчитель — той, хто реально провів урок (l.teacher), а не вчитель слоту розкладу (заміна йде тому, хто замінив)
    @Query("""
            SELECT DISTINCT l FROM Lesson l
            JOIN FETCH l.teacher t
            LEFT JOIN FETCH l.lessonsStudents
            WHERE l.storned = false
            AND l.date BETWEEN :start AND :end
            AND (:teacherId IS NULL OR t.id = :teacherId)
            """)
    List<Lesson> findAllForTeacherReport(LocalDate start, LocalDate end, Integer teacherId);

}
