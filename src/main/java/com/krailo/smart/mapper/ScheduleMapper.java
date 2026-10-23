package com.krailo.smart.mapper;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.Audience;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Schedule;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.repository.GangRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ScheduleMapper implements Mapper<Schedule, ScheduleDto> {

    private GangRepository gangRepository;

    @Override
    public Schedule mapDtoToEntityForCreate(ScheduleDto d) {
        Schedule e = new Schedule();
        e.setAudience(Audience.builder().id(d.getAudienceId()).build());
        // вчитель розкладу = вчитель групи, тому беремо групу з бази повністю
        Gang gang = findGang(d.getGangId());
        e.setGang(gang);
        e.setTeacher(gang.getTeacher());
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
        Gang gang = findGang(d.getGangId());
        e.setGang(gang);
        e.setTeacher(gang.getTeacher());
        e.setSubject(Subject.builder().id(d.getSubjectId()).build());
        e.setWeekDay(d.getWeekDay());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }

    private Gang findGang(Integer gangId) {
        return gangRepository.findById(gangId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Gang whith id= %d not exist", gangId)));
    }

}
