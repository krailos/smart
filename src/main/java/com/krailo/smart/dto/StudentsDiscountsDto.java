package com.krailo.smart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// пласкі поля замість сутностей: id — для форми (вибір у списках), назви — для показу.
// З форми приходять лише id і date, решта — null.
// @Builder — для порожньої форми; @AllArgsConstructor — щоб конструктор лишився public (див. TeacherDto)
@Value
@Builder
@AllArgsConstructor
public class StudentsDiscountsDto {

    private Integer id;
    // усі чотири поля форми в базі NOT NULL; порожній id раніше давав 500 (findById(null) у маппері)
    @NotNull(message = "Оберіть знижку")
    private Integer discountId;
    private String discountName;
    private Integer discountValue; // у відсотках
    @NotNull(message = "Оберіть учня")
    private Integer studentId;
    private String studentName;    // "Прізвище Ім'я"
    @NotNull(message = "Оберіть курс")
    private Integer subjectId;
    private String subjectName;
    @NotNull(message = "Вкажіть дату")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // формат <input type="date">: 2026-10-27
    private LocalDate date;

}
