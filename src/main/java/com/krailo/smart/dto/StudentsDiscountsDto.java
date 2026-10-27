package com.krailo.smart.dto;

import lombok.Value;

import java.time.LocalDate;

// пласкі поля замість сутностей: id — для форми (вибір у списках), назви — для показу.
// З форми приходять лише id і date, решта — null
@Value
public class StudentsDiscountsDto {

    private Integer id;
    private Integer discountId;
    private String discountName;
    private Integer discountValue; // у відсотках
    private Integer studentId;
    private String studentName;    // "Прізвище Ім'я"
    private Integer subjectId;
    private String subjectName;
    private LocalDate date;
}
