package com.krailo.smart.mapper;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.Schedule;
import com.krailo.smart.repository.AudienceRepository;
import com.krailo.smart.repository.GangRepository;
import com.krailo.smart.repository.SubjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ScheduleMapper implements Mapper<Schedule, ScheduleDto> {

    private AudienceRepository audienceRepository;
    private GangRepository gangRepository;
    private SubjectRepository subjectRepository;
 

    @Override
    public Schedule mapDtoToEntityForCreate(ScheduleDto d) {
        Schedule e = new Schedule();
        e.setAudience(audienceRepository.findById(d.getAudienceId()).get());
        e.setGang(gangRepository.findById(d.getGangId()).get());
        e.setSubject(subjectRepository.findById(d.getSubjectId()).get());
        e.setWeekDay(d.getWeekDay());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }

    @Override
    public ScheduleDto mapEntityToDto(Schedule e) {
        return new ScheduleDto(e.getId(), e.getAudience(), e.getAudience().getId(), e.getGang(), e.getGang().getId(),
                e.getSubject(), e.getSubject().getId(), e.getWeekDay(), e.getStartTime(), e.getEndTime());
    }

    @Override
    public Schedule mapDtoToEntityForUpdate(ScheduleDto d, Schedule e) {
        e.setAudience(audienceRepository.findById(d.getAudienceId()).get());
        e.setGang(gangRepository.findById(d.getGangId()).get());
        e.setSubject(subjectRepository.findById(d.getSubjectId()).get());
        e.setWeekDay(d.getWeekDay());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }

}
