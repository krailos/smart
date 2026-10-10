package com.krailo.smart.mapper;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.Schedule;
import com.krailo.smart.service.AudienceService;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.SubjectService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleMapper implements Mapper<Schedule, ScheduleDto> {

    private AudienceService audienceService;
    private GangService gangService;
    private SubjectService subjectService;


    @Override
    public Schedule mapDtoToEntityForCreate(ScheduleDto d) {
        Schedule e = new Schedule();
        e.setAudience(audienceService.findByIdEntity(d.getAudienceId()));
        e.setGang(gangService.findByIdEntity(d.getGangId()));
        e.setSubject(subjectService.findByIdEntity(d.getSubjectId()));
        e.setWeekDay(d.getWeekDay());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }

    @Override
    public ScheduleDto mapEntityToDto(Schedule e) {
        return new ScheduleDto(e.getId(), e.getAudience().getId(), e.getGang().getId(),
                e.getSubject().getId(), e.getWeekDay(), e.getStartTime(), e.getEndTime());
    }

    @Override
    public Schedule mapDtoToEntityForUpdate(ScheduleDto d, Schedule e) {
        e.setAudience(audienceService.findByIdEntity(d.getAudienceId()));
        e.setGang(gangService.findByIdEntity(d.getGangId()));
        e.setSubject(subjectService.findByIdEntity(d.getSubjectId()));
        e.setWeekDay(d.getWeekDay());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }

}
