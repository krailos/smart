package com.krailo.smart.repository;

import com.krailo.smart.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface DiscountRepository extends JpaRepository<Discount, Integer> {

    // archived = false — активні (для списків і форм), true — архів
    List<Discount> findAllByArchived(boolean archived);

    // у базі UNIQUE (discount_date, name): така сама назва з тією ж датою — дубль (id — та, що редагуємо, 0 — нова)
    boolean existsByNameAndDateAndIdNot(String name, LocalDate date, Integer id);

}
