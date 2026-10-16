package com.krailo.smart.dto;


import lombok.*;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonsStudentsDto {

    private Integer id;
    private Integer lessonId;
    private LocalDate lessonDate;
    private String subjectName;
    private Integer studentId;
    private String studentLastName;
    private String studentFirstName;
    private boolean present;
    private boolean payed;
}
