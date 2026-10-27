package com.krailo.smart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// @Builder — для порожньої форми нового вчителя (TeacherDto.builder().build());
// @AllArgsConstructor — щоб конструктор лишився public (з @Builder Lombok зробив би його закритим для інших пакетів)
@Value
@Builder
@AllArgsConstructor
public class TeacherDto {
    private Integer id;
    // межі — як у базі: first_name, last_name varchar(50) NOT NULL; решта необов'язкові
    @NotBlank(message = "Вкажіть ім'я")
    @Size(max = 50, message = "Не довше 50 символів")
    private String firstName;
    @Size(max = 50, message = "Не довше 50 символів")
    private String secondName;
    @NotBlank(message = "Вкажіть прізвище")
    @Size(max = 50, message = "Не довше 50 символів")
    private String lastName;
    @Size(max = 50, message = "Не довше 50 символів")
    private String phone;
    @Email(message = "Неправильний email")
    @Size(max = 50, message = "Не довше 50 символів")
    private String email;
    @Size(max = 150, message = "Не довше 150 символів")
    private String address;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // формат <input type="date">: 2026-10-27
    private LocalDate birthDate;
    @Size(max = 150, message = "Не довше 150 символів")
    private String description;
    // кількість груп — лише для списку вчителів; Integer, бо з форми вчителя цього поля немає — приходить null
    private Integer gangsCount;
}
