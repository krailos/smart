package com.krailo.smart.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
@Value
public class LessonDto {    
    
    private Integer id;
    private SubjectDto subjectDto;
    private GangDto gangDto;
    private AudienceDto audienceDto;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<LessonsStudentsDto> lessonsStudentsDto;

}
