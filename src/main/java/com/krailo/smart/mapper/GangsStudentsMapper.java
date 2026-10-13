package com.krailo.smart.mapper;

import com.krailo.smart.dto.GangsStudentsDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.GangsStudents;
import com.krailo.smart.entity.Student;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GangsStudentsMapper implements Mapper<GangsStudents, GangsStudentsDto>{

    @Override
    public GangsStudents mapDtoToEntityForCreate(GangsStudentsDto dto) {
        return GangsStudents.builder()
                .gang(Gang.builder().id(dto.getGangId()).build())
                .student(Student.builder().id(dto.getStudentId()).build())
                .build();
    }

    @Override
    public GangsStudentsDto mapEntityToDto(GangsStudents e) {
        return GangsStudentsDto.builder()
                .id(e.getId())
                .gangId(e.getGang().getId())
                .gangName(e.getGang().getName())
                .studentId(e.getStudent().getId())
                .studentLastName(e.getStudent().getLastName())
                .studentFirstName(e.getStudent().getFirstName())
                .build();
    }

    @Override
    public GangsStudents mapDtoToEntityForUpdate(GangsStudentsDto dto, GangsStudents e) {
        e.setGang(Gang.builder().id(dto.getGangId()).build());
        e.setStudent(Student.builder().id(dto.getStudentId()).build());
        return e;
    }
}
