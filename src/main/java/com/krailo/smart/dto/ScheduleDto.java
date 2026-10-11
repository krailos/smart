package com.krailo.smart.dto;

import com.krailo.smart.enumeration.WeekDay;
import lombok.Builder;
import lombok.Value;

import java.time.LocalTime;

@Builder
@Value
public class ScheduleDto {

    private int id;
    private AudienceDto audienceDto;
    private GangDto gangDto;
    private SubjectDto subjectDto;
    private WeekDay weekDay;
    private LocalTime startTime;
    private LocalTime endTime;

}
