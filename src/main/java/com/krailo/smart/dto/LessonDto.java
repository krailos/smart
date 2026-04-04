package com.krailo.smart.dto;

import com.krailo.smart.entity.Audience;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.entity.Subject;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Value
public class LessonDto {    
    
    private Integer id;
    private Subject subject;
    private Integer subjectId;
    private Gang gang;
    private Integer gangId;
    private Audience audience;
    private Integer audienceId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<LessonsStudents> lessonsStudents;
//    private List<Student> students;

    


}
