package com.krailo.smart.mapper;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.entity.Audience;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.service.AudienceService;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.SubjectService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LessonMapper implements Mapper<Lesson, LessonDto> {
    
    private SubjectService subjectService;
    private SubjectMapper subjectMapper;
    private GangService gangService;
    private GangMapper gangMapper;
    private AudienceService audienceService;
    private AudienceMapper audienceMapper;
    private LessonsStudentsMapper lessonsStudentsMapper;

    @Override
    public LessonDto mapEntityToDto(Lesson e) {
        return LessonDto.builder()
                .id(e.getId())
                .subjectId(e.getSubject().getId())
                .subjectName(e.getSubject().getName())
                .gangId(e.getGang().getId())
                .gangName(e.getGang().getName())
                .audienceId(e.getAudience().getId())
                .audienceName(e.getAudience().getName())
                .date(e.getDate())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .lessonsStudentsDto(e.getLessonsStudents().stream()
                        .map(lessonsStudentsMapper::mapEntityToDto).toList())
                .build();
    }


    @Override
    public Lesson mapDtoToEntityForCreate(LessonDto d) {
       Lesson e = new Lesson();
        e.setSubject(Subject.builder().id(d.getSubjectId()).build());
        // вчитель уроку = вчитель групи
        Gang gang = gangService.findByIdEntity(d.getGangId());
        e.setGang(gang);
        e.setTeacher(gang.getTeacher());
        e.setAudience(Audience.builder().id(d.getAudienceId()).build());
        e.setDate(d.getDate());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }


    @Override
    public Lesson mapDtoToEntityForUpdate(LessonDto d, Lesson e) {
        e.setSubject(Subject.builder().id(d.getSubjectId()).build());
        Gang gang = gangService.findByIdEntity(d.getGangId());
        e.setGang(gang);
        e.setTeacher(gang.getTeacher());
        e.setAudience(Audience.builder().id(d.getAudienceId()).build());
        e.setDate(d.getDate());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        return e;
    }

}
