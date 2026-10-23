package com.krailo.smart.dto;

import com.krailo.smart.enumeration.WeekDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {

    private int id;
    private Integer subjectId;
    private String subjectName;
    private Integer gangId;
    private String gangName;
    private Integer teacherId; // якщо не вибрано у формі — сервіс підставить основного вчителя групи
    private String teacherName;
    private Integer audienceId;
    private String audienceName;
    private WeekDay weekDay;
    // формат, який розуміє <input type="time"> (інакше Thymeleaf покаже "1:00 PM")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

}
