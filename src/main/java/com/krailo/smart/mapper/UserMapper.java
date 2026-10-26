package com.krailo.smart.mapper;

import com.krailo.smart.dto.UserDto;
import com.krailo.smart.entity.User;
import org.springframework.stereotype.Component;

// Тільки в один бік (база -> сторінка): користувача створюють і змінюють
// методи UserService, бо пароль треба хешувати, а роль і архів — перевіряти
@Component
public class UserMapper {

    public UserDto mapEntityToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getRole().name(),
                user.isArchived()
        );
    }
}
