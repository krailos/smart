package com.krailo.smart.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class PaymentDto {


    private Integer id;
    private String studentName; // "Прізвище Ім'я" — для списку й картки оплати
    private Integer studentId;
    private BigDecimal value;
    private LocalDate date;
    private String description;
    // Integer/Boolean (а не int/boolean): з форми нової оплати цих полів немає — приходить null
    private Integer stornoOfId; // це сторно оплати з цим id (сума від'ємна)
    private Boolean storned;    // цю оплату вже сторновано

}
