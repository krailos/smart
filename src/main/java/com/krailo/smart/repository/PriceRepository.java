package com.krailo.smart.repository;

import com.krailo.smart.entity.Price;
import com.krailo.smart.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;




public interface PriceRepository extends JpaRepository<Price, Integer> {
    
     List<Price> findBySubject (Subject subject);

}
