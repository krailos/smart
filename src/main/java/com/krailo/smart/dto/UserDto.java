package com.krailo.smart.dto;

import lombok.Value;

@Value
public class UserDto {
    private Integer id;
    private String name;
    private String password;
    private String role;
}
