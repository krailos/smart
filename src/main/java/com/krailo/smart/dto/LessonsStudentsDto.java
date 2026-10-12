package com.krailo.smart.dto;


import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonsStudentsDto {

    private Integer id;
    private Integer lessonId;
    private  StudentDto studentDto;
    private boolean present;
    private boolean payed;
}
