package com.krailo.smart.repository;

import com.krailo.smart.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DiscountRepository extends JpaRepository<Discount, Integer> {

    // archived = false — активні (для списків і форм), true — архів
    List<Discount> findAllByArchived(boolean archived);

}
