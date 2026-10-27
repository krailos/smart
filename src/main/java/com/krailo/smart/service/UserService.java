package com.krailo.smart.service;


import com.krailo.smart.dto.UserDto;
import com.krailo.smart.enumeration.Role;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.mapper.UserMapper;
import com.krailo.smart.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Transactional
@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    public static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_NAME_LENGTH = 50;

    private UserRepository userRepository;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private TeacherService teacherService;

    public List<UserDto> findAll(boolean archived) {
        return userRepository.findAllByArchivedOrderByName(archived).stream().map(userMapper::mapEntityToDto).toList();
    }

    // Вхід у застосунок: Spring шукає користувача тут, а пароль перевіряє сам (PasswordEncoder).
    // Архівного не знаходимо — для Spring це "невірний логін або пароль".
    // Регістр не важливий: увійти можна як "OLENA" чи "olena"
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByNameIgnoreCaseAndArchivedFalse(username).map(user -> new org.springframework.security.core.userdetails.User(
                user.getName(),   // логін як у базі, а не як набрали — далі (меню, «це ви», перевірки) він один
                user.getPassword(),
                Collections.singleton(user.getRole())
        )).orElseThrow(() -> new UsernameNotFoundException("failed to retrieve user: " + username));


    }

    // Новий користувач з тимчасовим паролем (адмін передає його людині)
    public void create(String name, String password, Role role, Integer teacherId) {
        String login = name == null ? "" : name.trim();
        if (login.isEmpty()) {
            throw new BusinessException("Вкажіть логін");
        }
        if (login.length() > MAX_NAME_LENGTH) {
            throw new BusinessException("Логін задовгий — не більше " + MAX_NAME_LENGTH + " символів");
        }
        if (login.contains(" ")) {
            throw new BusinessException("Логін без пробілів");
        }
        if (role == null) {
            throw new BusinessException("Оберіть роль");
        }
        Teacher teacher = teacherForRole(role, teacherId, null);
        checkPassword(password);
        // "Olena" і "olena" — той самий логін
        if (userRepository.existsByNameIgnoreCase(login)) {
            throw new BusinessException("Користувач з логіном «" + login + "» уже є "
                    + "(можливо, великими/малими літерами інакше або в архіві)");
        }
        userRepository.save(User.builder()
                .name(login)
                .password(passwordEncoder.encode(password)) // у базу — тільки хеш
                .role(role)
                .teacher(teacher)
                .mustChangePassword(true)   // пароль знає адмін — при першому вході користувач змінить його
                .build());
    }

    // Роль і (для вчителя) хто це з вчителів
    public void changeRole(Integer id, Role role, Integer teacherId, String currentName) {
        User user = findByIdEntity(id);
        if (role == null) {
            throw new BusinessException("Оберіть роль");
        }
        if (user.getName().equals(currentName)) {
            throw new BusinessException("Свою роль змінити не можна — попросіть іншого адміна");
        }
        Teacher teacher = teacherForRole(role, teacherId, user.getId());
        if (user.getRole() == Role.ADMIN && role != Role.ADMIN && !user.isArchived()) {
            requireAnotherAdmin();
        }
        user.setRole(role);
        user.setTeacher(teacher);
    }

    // Вчитель, з яким пов'язаний логін. Адмін — без вчителя.
    // Вчитель — обов'язково активна картка, і в одного вчителя лише один активний логін
    // (userId — той, кого змінюємо: його власна прив'язка конфліктом не є)
    private Teacher teacherForRole(Role role, Integer teacherId, Integer userId) {
        if (role != Role.TEACHER) {
            return null;
        }
        if (teacherId == null) {
            throw new BusinessException("Для ролі «Вчитель» оберіть, хто це з вчителів");
        }
        Teacher teacher = teacherService.findByIdEntity(teacherId);
        if (teacher.isArchived()) {
            throw new BusinessException("Вчитель в архіві — спершу відновіть його");
        }
        userRepository.findFirstByTeacherIdAndArchivedFalse(teacherId)
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> {
                    throw new BusinessException("У вчителя " + teacher.getLastName() + " " + teacher.getFirstName()
                            + " уже є логін «" + other.getName() + "»");
                });
        return teacher;
    }

    // Забули пароль: його не відновити (у базі хеш) — тільки задати новий, тимчасовий
    public void resetPassword(Integer id, String password, String currentName) {
        User user = findByIdEntity(id);
        if (user.getName().equals(currentName)) {
            throw new BusinessException("Свій пароль змініть на сторінці «Змінити мій пароль»");
        }
        checkPassword(password);
        user.setPassword(passwordEncoder.encode(password));
        user.setMustChangePassword(true);
    }

    // Користувач сам змінює свій пароль: спершу підтверджує поточний (щоб ніхто чужий
    // не змінив його за незаблокованим комп'ютером), новий — двічі, щоб не помилитись
    public void changeOwnPassword(String name, String currentPassword, String newPassword, String repeatPassword) {
        User user = userRepository.findUserByNameIgnoreCaseAndArchivedFalse(name)
                .orElseThrow(() -> new EntityNotFoundException("User " + name + " not exist"));
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("Поточний пароль введено неправильно");
        }
        checkPassword(newPassword);
        if (!newPassword.equals(repeatPassword)) {
            throw new BusinessException("Новий пароль і повтор не збігаються");
        }
        if (newPassword.equals(currentPassword)) {
            throw new BusinessException("Новий пароль має відрізнятися від поточного");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
    }

    public boolean mustChangePassword(String name) {
        return userRepository.findUserByNameIgnoreCaseAndArchivedFalse(name).map(User::isMustChangePassword).orElse(false);
    }

    public void archive(Integer id, String currentName) {
        User user = findByIdEntity(id);
        if (user.getName().equals(currentName)) {
            throw new BusinessException("Себе в архів перемістити не можна");
        }
        if (user.getRole() == Role.ADMIN && !user.isArchived()) {
            requireAnotherAdmin();
        }
        user.setArchived(true);
    }

    public void restore(Integer id) {
        User user = findByIdEntity(id);
        // поки логін був в архіві, вчитель міг отримати інший логін
        if (user.getTeacher() != null) {
            teacherForRole(user.getRole(), user.getTeacher().getId(), user.getId());
        }
        user.setArchived(false);
    }

    private User findByIdEntity(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id= %d not exist", id)));
    }

    private void checkPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException("Пароль має бути не коротший за " + MIN_PASSWORD_LENGTH + " символів");
        }
    }

    // цей адмін — останній активний? тоді не можна ні архівувати його, ні зробити вчителем
    private void requireAnotherAdmin() {
        if (userRepository.countByRoleAndArchivedFalse(Role.ADMIN) <= 1) {
            throw new BusinessException("Це останній адміністратор — спочатку призначте іншого");
        }
    }
}
