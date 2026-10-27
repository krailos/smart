package com.krailo.smart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Value
public class DiscountDto {


    private Integer id;
    // межі — як у базі: name varchar(50) NOT NULL, discount_date NOT NULL
    @NotBlank(message = "Вкажіть вид знижки")
    @Size(max = 50, message = "Вид знижки — не довше 50 символів")
    private String name;
    // Integer (а не int): порожнє поле форми — null, і @NotNull скаже про це зрозуміло
    @NotNull(message = "Вкажіть розмір знижки")
    @Min(value = 1, message = "Знижка — від 1 до 100%")
    @Max(value = 100, message = "Знижка — від 1 до 100%")
    private Integer value;
    @NotNull(message = "Вкажіть дату, з якої діє знижка")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // формат <input type="date">: 2026-10-27
    private LocalDate date;

}
