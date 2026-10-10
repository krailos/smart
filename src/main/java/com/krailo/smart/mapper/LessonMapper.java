package com.krailo.smart.mapper;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.service.AudienceService;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.SubjectService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LessonMapper implements Mapper<Lesson, LessonDto> {
    
    private SubjectService subjectService;
    private GangService gangService;
    private AudienceService audienceService;
    private LessonsStudentsMapper lessonsStudentsMapper;

    @Override
    public LessonDto mapEntityToDto(Lesson e) {
        return LessonDto.builder()
                .id(e.getId())
                .subjectId(e.getSubject().getId())
                .gangId(e.getGang().getId())
                .audienceId(e.getAudience().getId())
                .date(e.getDate())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .lessonsStudentsDto(e.getLessonsStudents().stream().map(lessonsStudentsMapper::mapEntityToDto).toList())
                .build();
    }


    @Override
    public Lesson mapDtoToEntityForCreate(LessonDto d) {
       Lesson e = new Lesson();
        e.setSubject(subjectService.findByIdEntity(d.getSubjectId()));
        e.setGang(gangService.findByIdEntity(d.getGangId()));
        e.setAudience(audienceService.findByIdEntity(d.getAudienceId()));
        e.setDate(d.getDate());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }


    @Override
    public Lesson mapDtoToEntityForUpdate(LessonDto d, Lesson e) {
        e.setSubject(subjectService.findByIdEntity(d.getSubjectId()));
        e.setGang(gangService.findByIdEntity(d.getGangId()));
        e.setAudience(audienceService.findByIdEntity(d.getAudienceId()));
        e.setDate(d.getDate());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }

}
