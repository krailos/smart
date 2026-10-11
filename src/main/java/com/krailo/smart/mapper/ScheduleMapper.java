package com.krailo.smart.mapper;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.Schedule;
import com.krailo.smart.service.AudienceService;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.SubjectService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ScheduleMapper implements Mapper<Schedule, ScheduleDto> {

    private SubjectService subjectService;
    private SubjectMapper subjectMapper;
    private GangService gangService;
    private GangMapper gangMapper;
    private AudienceService audienceService;
    private AudienceMapper audienceMapper;


    @Override
    public Schedule mapDtoToEntityForCreate(ScheduleDto d) {
        Schedule e = new Schedule();
        e.setAudience(audienceService.findByIdEntity(d.getAudienceDto().getId()));
        e.setGang(gangService.findByIdEntity(d.getGangDto().getId()));
        e.setSubject(subjectService.findByIdEntity(d.getSubjectDto().getId()));
        e.setWeekDay(d.getWeekDay());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }

    @Override
    public ScheduleDto mapEntityToDto(Schedule e) {
        return ScheduleDto.builder()
                .id(e.getId())
                .audienceDto(audienceMapper.mapEntityToDto(e.getAudience()))
                .gangDto(gangMapper.mapEntityToDto(e.getGang()))
                .subjectDto(subjectMapper.mapEntityToDto(e.getSubject()))
                .weekDay(e.getWeekDay())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .build();
    }

    @Override
    public Schedule mapDtoToEntityForUpdate(ScheduleDto d, Schedule e) {
        e.setAudience(audienceService.findByIdEntity(d.getAudienceDto().getId()));
        e.setGang(gangService.findByIdEntity(d.getGangDto().getId()));
        e.setSubject(subjectService.findByIdEntity(d.getSubjectDto().getId()));
        e.setWeekDay(d.getWeekDay());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }

}
