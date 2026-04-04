package com.krailo.smart.mapper;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.repository.AudienceRepository;
import com.krailo.smart.repository.GangRepository;
import com.krailo.smart.repository.SubjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LessonMapper implements Mapper<Lesson, LessonDto> {
    
    private SubjectRepository subjectRepository;
    private GangRepository gangRepository;
    private AudienceRepository audienceRepository;

    @Override
    public Lesson mapDtoToEntityForCreate(LessonDto d) {
       Lesson e = new Lesson();
        e.setSubject(subjectRepository.findById(d.getSubjectId()).get());
        e.setGang(gangRepository.findById(d.getGangId()).get()); 
        e.setAudience(audienceRepository.findById(d.getAudienceId()).get());
        e.setDate(d.getDate());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        e.setLessonsStudents(d.getLessonsStudents());
        return e;
    }

    @Override
    public LessonDto mapEntityToDto(Lesson e) {
        return new LessonDto(
                e.getId(), 
                e.getSubject(), 
                e.getSubject().getId(), 
                e.getGang(),
                e.getGang().getId(),
                e.getAudience(),
                e.getAudience().getId(),
                e.getDate(),
                e.getStartTime(),
                e.getEndTime(),
                e.getLessonsStudents());     
    }

    @Override
    public Lesson mapDtoToEntityForUpdate(LessonDto d, Lesson e) {   
        e.setSubject(subjectRepository.findById(d.getSubjectId()).get());
        e.setGang(gangRepository.findById(d.getGangId()).get()); 
        e.setAudience(audienceRepository.findById(d.getAudienceId()).get());
        e.setDate(d.getDate());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        e.setLessonsStudents(d.getLessonsStudents());
        return e;
    }

}
