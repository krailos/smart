package com.krailo.smart.mapper;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.Audience;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Schedule;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.entity.Teacher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ScheduleMapper implements Mapper<Schedule, ScheduleDto> {


    @Override
    public Schedule mapDtoToEntityForCreate(ScheduleDto d) {
        Schedule e = new Schedule();
        e.setAudience(Audience.builder().id(d.getAudienceId()).build());
        e.setGang(Gang.builder().id(d.getGangId()).build());
        e.setTeacher(Teacher.builder().id(d.getTeacherId()).build());
        e.setSubject(Subject.builder().id(d.getSubjectId()).build());
        e.setWeekDay(d.getWeekDay());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }

    @Override
    public ScheduleDto mapEntityToDto(Schedule e) {
        return ScheduleDto.builder()
                .id(e.getId())
                .audienceId(e.getAudience().getId())
                .audienceName(e.getAudience().getName())
                .gangId(e.getGang().getId())
                .gangName((e.getGang().getName()))
                .teacherId(e.getTeacher().getId())
                .teacherName(e.getTeacher().getLastName() + " " + e.getTeacher().getFirstName())
                .subjectId(e.getSubject().getId())
                .subjectName(e.getSubject().getName())
                .weekDay(e.getWeekDay())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .build();
    }

    @Override
    public Schedule mapDtoToEntityForUpdate(ScheduleDto d, Schedule e) {
        e.setAudience(Audience.builder().id(d.getAudienceId()).build());
        e.setGang(Gang.builder().id(d.getGangId()).build());
        e.setTeacher(Teacher.builder().id(d.getTeacherId()).build());
        e.setSubject(Subject.builder().id(d.getSubjectId()).build());
        e.setWeekDay(d.getWeekDay());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }

}
