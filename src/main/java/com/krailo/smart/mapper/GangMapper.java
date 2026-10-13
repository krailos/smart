package com.krailo.smart.mapper;

import com.krailo.smart.dto.GangDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.entity.Teacher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GangMapper implements Mapper<Gang, GangDto> {

    GangsStudentsMapper gangsStudentsMapper;
    @Override
    public GangDto mapEntityToDto(Gang o) {
        return GangDto.builder().id(o.getId())
                .name(o.getName())
                .description(o.getDescription())
                .subjectId(o.getSubject().getId())
                .subjectNme(o.getSubject().getName())
                .teacherId(o.getTeacher().getId())
                .teacherLastName(o.getTeacher().getLastName())
                .teacherFirstName(o.getTeacher().getFirstName())
                .gangsStudentsDto(o.getGangStudents().stream().map(gangsStudentsMapper::mapEntityToDto).toList())
                .build();
    }

    @Override
    public Gang mapDtoToEntityForCreate(GangDto d) {
       Gang e = new Gang();
       e.setName(d.getName());
       e.setDescription(d.getDescription());
       e.setSubject(Subject.builder().id(d.getSubjectId()).build());
       e.setTeacher(Teacher.builder().id(d.getTeacherId()).build());
        return e;
    }

    @Override
    public Gang mapDtoToEntityForUpdate(GangDto d, Gang e) {
        e.setId(d.getId());
        e.setName(d.getName());
        e.setDescription(d.getDescription());
        e.setSubject(Subject.builder().id(d.getSubjectId()).build());
        e.setTeacher(Teacher.builder().id(d.getTeacherId()).build());
        return e;
    }

}
