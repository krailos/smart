package com.krailo.smart.mapper;

import com.krailo.smart.dto.GangDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.service.SubjectService;
import com.krailo.smart.service.TeacherService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GangMapper implements Mapper<Gang, GangDto> {
    
    SubjectService subjectService;
    TeacherService teacherService;
    GangsStudentsMapper gangsStudentsMapper;

    @Override
    public GangDto mapEntityToDto(Gang o) {
        return GangDto.builder().id(o.getId()).name(o.getName()).description(o.getDescription())
                .subjectId(o.getSubject().getId()).teacherId(o.getTeacher().getId())
                .gangsStudentsDto(o.getGangStudents().stream().map(gangsStudentsMapper::mapEntityToDto).toList())
                .build();
    }

    @Override
    public Gang mapDtoToEntityForCreate(GangDto d) {
       Gang e = new Gang();
       e.setName(d.getName());
       e.setDescription(d.getDescription());
       e.setSubject(subjectService.findByIdEntity(d.getSubjectId()));
       e.setTeacher(teacherService.findByIdEntity(d.getTeacherId()));
        return e;
    }

    @Override
    public Gang mapDtoToEntityForUpdate(GangDto d, Gang e) {
        e.setId(d.getId());
        e.setName(d.getName());
        e.setDescription(d.getDescription());
        e.setSubject(subjectService.findByIdEntity(d.getSubjectId()));
        e.setTeacher(teacherService.findByIdEntity(d.getTeacherId()));
        return e;
    }

}
