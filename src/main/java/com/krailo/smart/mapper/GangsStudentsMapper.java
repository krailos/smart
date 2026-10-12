package com.krailo.smart.mapper;

import com.krailo.smart.dto.GangsStudentsDto;
import com.krailo.smart.entity.GangsStudents;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GangsStudentsMapper implements Mapper<GangsStudents, GangsStudentsDto>{

    private GangService gangService;
    private StudentService studentService;
    private StudentMapper studentMapper;

    @Override
    public GangsStudents mapDtoToEntityForCreate(GangsStudentsDto dto) {
        return GangsStudents.builder()
                .gang(gangService.findByIdEntity(dto.getGangId()))
                .student(studentService.findByIdEntity(dto.getStudentDto().getId()))
                .build();
    }

    @Override
    public GangsStudentsDto mapEntityToDto(GangsStudents e) {
        return GangsStudentsDto.builder()
                .id(e.getId())
                .gangId(e.getGang().getId())
                .studentDto(studentMapper.mapEntityToDto(e.getStudent()))
                .build();
    }

    @Override
    public GangsStudents mapDtoToEntityForUpdate(GangsStudentsDto dto, GangsStudents e) {
        e.setGang(gangService.findByIdEntity(dto.getGangId()));
        e.setStudent(studentService.findByIdEntity(dto.getStudentDto().getId()));
        return e;
    }
}
