package com.krailo.smart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class PaymentDto {


    private Integer id;
    private String studentName; // "Прізвище Ім'я" — для списку й картки оплати
    // Перевірки форми нової оплати (спрацьовують лише там, де в контролері стоїть @Valid).
    // Межі — як у базі: student_id і payment_date NOT NULL, payment_value numeric(10,2), description varchar(150)
    @NotNull(message = "Оберіть учня")
    private Integer studentId;
    @NotNull(message = "Вкажіть суму")
    @DecimalMin(value = "0.01", message = "Сума оплати має бути більшою за нуль")
    @Digits(integer = 8, fraction = 2, message = "Сума — до 8 цифр перед комою і не більше 2 після")
    private BigDecimal value;
    // th:field виводить дату у форматі, який розуміє <input type="date">: 2026-10-27 (без цього — 10/27/26 і порожнє поле)
    @NotNull(message = "Вкажіть дату")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
    @Size(max = 150, message = "Примітка — не довше 150 символів")
    private String description;
    // Integer/Boolean (а не int/boolean): з форми нової оплати цих полів немає — приходить null
    private Integer stornoOfId; // це сторно оплати з цим id (сума від'ємна)
    private Boolean storned;    // цю оплату вже сторновано
    private Boolean openingBalance; // це початковий баланс учня (або його сторно), а не отримані гроші

}
