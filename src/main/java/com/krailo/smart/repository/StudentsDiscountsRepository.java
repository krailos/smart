package com.krailo.smart.repository;

import com.krailo.smart.entity.StudentsDiscounts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudentsDiscountsRepository extends JpaRepository<StudentsDiscounts, Integer>{

    // чи вже є (не видалена) така знижка учня на цей курс — замість UNIQUE у БД
    boolean existsByStudentIdAndDiscountIdAndSubjectId(Integer studentId, Integer discountId, Integer subjectId);

    // те саме при редагуванні — крім самого запису, який редагуємо
    boolean existsByStudentIdAndDiscountIdAndSubjectIdAndIdNot(Integer studentId, Integer discountId, Integer subjectId, Integer id);

    // чи вже є в учня (будь-яка) знижка на цей курс з цієї дати: на уроці діє одна, найновіша, —
    // дві з однаковою датою зробили б вибір випадковим
    boolean existsByStudentIdAndSubjectIdAndDate(Integer studentId, Integer subjectId, LocalDate date);

    boolean existsByStudentIdAndSubjectIdAndDateAndIdNot(Integer studentId, Integer subjectId, LocalDate date, Integer id);

    // знижки одного учня (сторінка /students/{id}/discounts)
    List<StudentsDiscounts> findAllByStudentId(Integer studentId);
}
