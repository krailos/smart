package com.krailo.smart.dto;


import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class LessonsStudentsDto {

    private Integer id;
    private Integer lessonId;
    private  Integer studentId;
    private boolean present;
    private boolean payed;
}
