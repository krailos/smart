package com.krailo.smart.repository;

import com.krailo.smart.entity.Price;
import com.krailo.smart.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;






public interface PriceRepository extends JpaRepository<Price, Integer> {
    
     List<Price> findBySubject (Subject subject);

     // чи вже є (не видалена) ціна курсу на цю дату — замість UNIQUE (subject_id, price_date) у БД
     boolean existsBySubjectIdAndDate(Integer subjectId, LocalDate date);

}
