package com.krailo.smart.repository;

import com.krailo.smart.entity.User;
import com.krailo.smart.enumeration.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // вхід: тільки активні (архівний користувач увійти не може); регістр не важливий — Olena = olena
    Optional<User> findUserByNameIgnoreCaseAndArchivedFalse(String name);

    List<User> findAllByArchivedOrderByName(boolean archived);

    // логін унікальний серед УСІХ, і архівних теж, без урахування регістру (у базі — UNIQUE lower(name), V25)
    boolean existsByNameIgnoreCase(String name);

    // активний логін цього вчителя (у вчителя — не більше одного)
    Optional<User> findFirstByTeacherIdAndArchivedFalse(Integer teacherId);

    // скільки активних адмінів — щоб не лишитись без жодного
    long countByRoleAndArchivedFalse(Role role);
}
