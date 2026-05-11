package com.krailo.smart.mapper;

import com.krailo.smart.dto.UserDto;
import com.krailo.smart.entity.User;
import com.krailo.smart.enumeration.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements Mapper<User, UserDto> {


    @Override
    public User mapDtoToEntityForCreate(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setPassword(userDto.getPassword());
        user.setRole(Role.valueOf(userDto.getRole()));
        return user;
    }

    @Override
    public UserDto mapEntityToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getPassword(),
                user.getRole().name()
        );
    }

    @Override
    public User mapDtoToEntityForUpdate(UserDto userDto, User user) {
        user.setName(userDto.getName());
        user.setPassword(userDto.getPassword());
        user.setRole(Role.valueOf(userDto.getRole()));
        return user;
    }
}
