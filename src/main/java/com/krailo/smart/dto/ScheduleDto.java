package com.krailo.smart.dto;

import com.krailo.smart.enumeration.WeekDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {

    private int id;
    private AudienceDto audienceDto;
    private GangDto gangDto;
    private SubjectDto subjectDto;
    private WeekDay weekDay;
    private LocalTime startTime;
    private LocalTime endTime;

}
