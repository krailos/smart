package com.krailo.smart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {    
    
    private Integer id;
    private Integer scheduleId; // з якого слоту розкладу проведено урок
    private Integer subjectId;
    private String subjectName;
    private Integer gangId;
    private String gangName;
    private Integer teacherId; // хто ФАКТИЧНО провів урок (може бути заміна) — з цього рахується зарплата
    private String teacherName;
    private Integer audienceId;
    private String audienceName;
    // формати, які розуміють <input type="date"> і <input type="time">
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;
    private List<LessonsStudentsDto> lessonsStudentsDto;
    private boolean storned; // урок сторновано

}
