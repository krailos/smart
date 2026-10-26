package com.krailo.smart.mapper;

import com.krailo.smart.dto.UserDto;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.entity.User;
import org.springframework.stereotype.Component;

// Тільки в один бік (база -> сторінка): користувача створюють і змінюють
// методи UserService, бо пароль треба хешувати, а роль і архів — перевіряти
@Component
public class UserMapper {

    public UserDto mapEntityToDto(User user) {
        Teacher teacher = user.getTeacher();
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getRole().name(),
                teacher == null ? null : teacher.getId(),
                teacher == null ? null : teacher.getLastName() + " " + teacher.getFirstName(),
                user.isMustChangePassword(),
                user.isArchived()
        );
    }
}
