package com.krailo.smart.dto;

import lombok.Value;

// Користувач для сторінки. Пароля тут немає навіть у вигляді хеша:
// він приходить тільки з форми окремим полем і одразу хешується (UserService)
@Value
public class UserDto {
    private Integer id;
    private String name;
    private String role;
    private boolean archived;
}
