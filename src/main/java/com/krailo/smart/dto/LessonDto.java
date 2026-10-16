package com.krailo.smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {    
    
    private Integer id;
    private Integer subjectId;
    private String subjectName;
    private Integer gangId;
    private String gangName;
    private Integer audienceId;
    private String audienceName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<LessonsStudentsDto> lessonsStudentsDto;

}
