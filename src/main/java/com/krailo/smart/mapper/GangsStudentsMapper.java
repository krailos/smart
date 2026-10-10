package com.krailo.smart.mapper;

import com.krailo.smart.dto.GangsStudentsDto;
import com.krailo.smart.entity.GangsStudents;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.StudentService;
import org.springframework.stereotype.Component;

@Component
public class GangsStudentsMapper implements Mapper<GangsStudents, GangsStudentsDto>{

    private GangService gangService;
    private StudentService studentService;

    @Override
    public GangsStudents mapDtoToEntityForCreate(GangsStudentsDto dto) {
        return GangsStudents.builder()
                .gang(gangService.findByIdEntity(dto.getGangId()))
                .student(studentService.findByIdEntity(dto.getStudentId()))
                .build();
    }

    @Override
    public GangsStudentsDto mapEntityToDto(GangsStudents e) {
        return GangsStudentsDto.builder()
                .id(e.getId())
                .gangId(e.getGang().getId())
                .studentId(e.getStudent().getId())
                .build();
    }

    @Override
    public GangsStudents mapDtoToEntityForUpdate(GangsStudentsDto dto, GangsStudents e) {
        e.setGang(gangService.findByIdEntity(dto.getGangId()));
        e.setStudent(studentService.findByIdEntity(dto.getStudentId()));
        return e;
    }
}
