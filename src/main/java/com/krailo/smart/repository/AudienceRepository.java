package com.krailo.smart.repository;

import com.krailo.smart.entity.Audience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AudienceRepository extends JpaRepository<Audience, Integer> {

    // archived = false — активні (для списків і форм), true — архів
    List<Audience> findAllByArchived(boolean archived);

}
