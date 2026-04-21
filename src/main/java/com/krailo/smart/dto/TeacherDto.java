package com.krailo.smart.dto;

import com.krailo.smart.entity.Gang;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
public class TeacherDto {
    private Integer id;
    private String firstName;
    private String secondName;
    private String lastName;
    private String phone;
    private String email;
    private String address;
    private LocalDate birthDate;
    private String description;
    private List<Gang> gangs;

}
