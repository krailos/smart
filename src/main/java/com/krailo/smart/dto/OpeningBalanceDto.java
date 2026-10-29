package com.krailo.smart.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

// Форма "Початковий баланс" на картці учня. Окремо від PaymentDto: тут сума може бути від'ємною (борг),
// а звичайна оплата — тільки більша за нуль. "Не нуль", "один раз" і "не пізніше першої операції"
// перевіряє PaymentReceivingService.receiveOpeningBalance
@Value
public class OpeningBalanceDto {

    @NotNull(message = "Вкажіть суму")
    @Digits(integer = 8, fraction = 2, message = "Сума — до 8 цифр перед комою і не більше 2 після")
    private BigDecimal value;
    @NotNull(message = "Вкажіть дату")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
    @Size(max = 150, message = "Примітка — не довше 150 символів")
    private String description;

}
