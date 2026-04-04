package com.krailo.smart.mapper;

import com.krailo.smart.dto.SubjectDto;
import com.krailo.smart.entity.Subject;
import org.springframework.stereotype.Component;


@Component
public class SubjectMapper implements Mapper<Subject, SubjectDto> {

    @Override
    public SubjectDto mapEntityToDto(Subject entity) {

        return new SubjectDto(entity.getId(), entity.getName(), entity.getDescription(), entity.getPrices());
    }

    @Override
    public Subject mapDtoToEntityForCreate(SubjectDto dto) {
        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
        subject.setPrices(dto.getPrices());
        return subject;
    }

    @Override
    public Subject mapDtoToEntityForUpdate(SubjectDto dto, Subject entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }

}
