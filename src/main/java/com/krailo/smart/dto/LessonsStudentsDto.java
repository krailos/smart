package com.krailo.smart.dto;


import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonsStudentsDto {

    private Integer id;
    private Integer lessonId;
    private Integer studentId;
    private String studentLastName;
    private String studentFirstName;

    private boolean present;
    private boolean payed;
}
