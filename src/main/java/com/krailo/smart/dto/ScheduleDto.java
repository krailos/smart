package com.krailo.smart.dto;

import com.krailo.smart.entity.Audience;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.enumeration.WeekDay;
import lombok.Value;

import java.time.LocalTime;

@Value
public class ScheduleDto {
    
    private int id;
    private Audience audience;
    private Integer audienceId;
    private Gang gang;
    private Integer gangId;
    private Subject subject;
    private Integer subjectId;
    private WeekDay weekDay;
    private LocalTime startTime;
    private LocalTime endTime;

}
