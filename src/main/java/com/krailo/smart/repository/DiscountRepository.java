package com.krailo.smart.repository;

import com.krailo.smart.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;



public interface DiscountRepository extends JpaRepository<Discount, Integer> {

}
