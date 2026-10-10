package com.krailo.smart.mapper;

import com.krailo.smart.dto.PriceDto;
import com.krailo.smart.entity.Price;
import com.krailo.smart.repository.SubjectRepository;
import com.krailo.smart.service.SubjectService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PriceMapper implements Mapper<Price, PriceDto> {

    SubjectRepository subjectRepository;
    SubjectService subjectService;

    @Override
    public PriceDto mapEntityToDto(Price entity) {

        return PriceDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .value(entity.getValue())
                .date(entity.getDate())
                .subjectId(entity.getSubject().getId())
                .build();
    }

    @Override
    public Price mapDtoToEntityForCreate(PriceDto dto) {
        Price entity = new Price();
        entity.setName(dto.getName());
        entity.setValue(dto.getValue());
        entity.setDate(dto.getDate());
        entity.setSubject(subjectService.findByIdEntity(dto.getSubjectId()));
        return entity;
    }

    @Override
    public Price mapDtoToEntityForUpdate(PriceDto dto, Price entity) {
        entity.setName(dto.getName());
        entity.setValue(dto.getValue());
        entity.setDate(dto.getDate());
        entity.setSubject(subjectService.findByIdEntity(dto.getSubjectId()));
        return entity;
    }

}
