package com.krailo.smart.dto;

import lombok.Value;

import java.util.List;

// Сторінка вчителя "Мої учні": група і її учні — тільки ім'я та контакт (без балансу й оплат)
@Value
public class TeacherGangDto {
    private String gangName;
    private String subjectName;
    private List<Student> students;

    @Value
    public static class Student {
        private String lastName;
        private String firstName;
        private String contactName;
        private String phone;
    }
}
