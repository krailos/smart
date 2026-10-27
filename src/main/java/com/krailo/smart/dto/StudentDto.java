package com.krailo.smart.dto;

import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

// @Builder — для порожньої форми нового учня (StudentDto.builder().build());
// @AllArgsConstructor — щоб конструктор лишився public (з @Builder Lombok зробив би його закритим для інших пакетів)
@Value
@Builder
@AllArgsConstructor
public class StudentDto {
    private Integer id;
    // межі — як у базі: first_name, last_name, contact_name, phone, gender, student_status NOT NULL;
    // рядки varchar(50), адреса й опис varchar(150)
    @NotBlank(message = "Вкажіть ім'я")
    @Size(max = 50, message = "Не довше 50 символів")
    private String firstName;
    @Size(max = 50, message = "Не довше 50 символів")
    private String secondName;
    @NotBlank(message = "Вкажіть прізвище")
    @Size(max = 50, message = "Не довше 50 символів")
    private String lastName;
    @NotBlank(message = "Вкажіть контактну особу")
    @Size(max = 50, message = "Не довше 50 символів")
    private String contactName;
    @NotBlank(message = "Вкажіть телефон")
    @Size(max = 50, message = "Не довше 50 символів")
    private String phone;
    @Email(message = "Неправильний email")
    @Size(max = 50, message = "Не довше 50 символів")
    private String email;
    @Size(max = 150, message = "Не довше 150 символів")
    private String address;
    @NotNull(message = "Оберіть стать")
    private Gender gender;
    @NotNull(message = "Оберіть статус")
    private StudentStatus studentStatus;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // формат <input type="date">: 2026-10-27
    private LocalDate birthDate;
    @Size(max = 150, message = "Не довше 150 символів")
    private String description;
    private BigDecimal balance; // може бути null: у формах учня цього поля немає
    // кількості — лише для списку учнів (StudentService.findAllByPage); Integer, бо з форм учня приходить null
    private Integer gangsCount;
    private Integer discountsCount;
}
