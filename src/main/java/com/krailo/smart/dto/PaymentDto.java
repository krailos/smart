package com.krailo.smart.dto;

import com.krailo.smart.entity.Student;
import lombok.Value;

import java.time.LocalDate;

@Value
public class PaymentDto {


    private Integer id;
    private Student student;
    private Integer studentId;
    private int value;
    private LocalDate date;
    private String description;
    // Integer/Boolean (а не int/boolean): з форми нової оплати цих полів немає — приходить null
    private Integer stornoOfId; // це сторно оплати з цим id (сума від'ємна)
    private Boolean storned;    // цю оплату вже сторновано

}
