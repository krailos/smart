package com.krailo.smart.repository;

import com.krailo.smart.entity.StudentsDiscounts;
import org.springframework.data.jpa.repository.JpaRepository;




public interface StudentsDiscountsRepository extends JpaRepository<StudentsDiscounts, Integer>{

    // чи вже є (не видалена) така знижка учня на цей курс — замість UNIQUE у БД
    boolean existsByStudentIdAndDiscountIdAndSubjectId(Integer studentId, Integer discountId, Integer subjectId);
}
