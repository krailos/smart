package com.krailo.smart.dto;

import lombok.Value;

import java.time.LocalDate;

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
    // кількість груп — лише для списку вчителів; Integer, бо з форми вчителя цього поля немає — приходить null
    private Integer gangsCount;

}
