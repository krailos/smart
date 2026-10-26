package com.krailo.smart.repository;

import com.krailo.smart.entity.User;
import com.krailo.smart.enumeration.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // вхід: тільки активні (архівний користувач увійти не може)
    Optional<User> findUserByNameAndArchivedFalse(String name);

    List<User> findAllByArchivedOrderByName(boolean archived);

    // логін унікальний серед УСІХ, і архівних теж (UNIQUE у базі, V22)
    boolean existsByName(String name);

    // скільки активних адмінів — щоб не лишитись без жодного
    long countByRoleAndArchivedFalse(Role role);
}
