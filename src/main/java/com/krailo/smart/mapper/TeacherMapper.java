package com.krailo.smart.mapper;

import com.krailo.smart.dto.TeacherDto;
import com.krailo.smart.entity.Teacher;
import org.springframework.stereotype.Component;

@Component
public class TeacherMapper implements Mapper<Teacher, TeacherDto> {
    @Override
    public Teacher mapDtoToEntityForCreate(TeacherDto dto) {
        Teacher e = new Teacher();
        e.setFirstName(dto.getFirstName());
        e.setSecondName(dto.getSecondName());
        e.setLastName(dto.getLastName());
        e.setPhone(dto.getPhone());
        e.setEmail(dto.getEmail());
        e.setAddress(dto.getAddress());
        e.setBirthDate(dto.getBirthDate());
        e.setDescription(dto.getDescription());
        return e;
    }

    @Override
    public TeacherDto mapEntityToDto(Teacher e) {
        return new TeacherDto(
                e.getId(),
                e.getFirstName(),
                e.getSecondName(),
                e.getLastName(),
                e.getPhone(),
                e.getEmail(),
                e.getAddress(),
                e.getBirthDate(),
                e.getDescription(),
                e.getGangs()
        );
    }

    @Override
    public Teacher mapDtoToEntityForUpdate(TeacherDto dto, Teacher e) {
        e.setFirstName(dto.getFirstName());
        e.setSecondName(dto.getSecondName());
        e.setLastName(dto.getLastName());
        e.setPhone(dto.getPhone());
        e.setEmail(dto.getEmail());
        e.setAddress(dto.getAddress());
        e.setBirthDate(dto.getBirthDate());
        e.setDescription(dto.getDescription());
        return e;
    }
}
