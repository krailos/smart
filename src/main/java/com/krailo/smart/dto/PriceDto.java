package com.krailo.smart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Value
public class PriceDto {

    private Integer id;
    // межі — як у базі: name varchar(50), price_value numeric(10,2) NOT NULL, price_date і subject_id NOT NULL
    @Size(max = 50, message = "Вид ціни — не довше 50 символів")
    private String name;
    // 0 дозволено (безкоштовний урок), від'ємна — ні
    @NotNull(message = "Вкажіть вартість уроку")
    @DecimalMin(value = "0", message = "Вартість уроку не може бути від'ємною")
    @Digits(integer = 8, fraction = 2, message = "Вартість — до 8 цифр перед комою і не більше 2 після")
    private BigDecimal value;
    @NotNull(message = "Вкажіть дату, з якої діє ціна")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // формат <input type="date">: 2026-10-27
    private LocalDate date;
    @NotNull(message = "Не вказано курс") // приховане поле форми — порожнє лише якщо форму підробили
    private Integer subjectId;
    private String subjectName;


}
