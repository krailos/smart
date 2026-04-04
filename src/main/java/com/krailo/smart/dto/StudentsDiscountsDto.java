package com.krailo.smart.dto;

import com.krailo.smart.entity.Discount;
import com.krailo.smart.entity.Student;
import com.krailo.smart.entity.Subject;
import lombok.Value;

import java.time.LocalDate;

@Value
public class StudentsDiscountsDto {

    private Integer id;
    private Discount discount;
    private Integer discountId;
    private Student student;
    private Integer studentId;
    private Subject subject;
    private Integer subjectId;
    private LocalDate date;
}
