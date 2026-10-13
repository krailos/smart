package com.krailo.smart.mapper;

import com.krailo.smart.dto.PriceDto;
import com.krailo.smart.entity.Price;
import com.krailo.smart.entity.Subject;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class PriceMapper implements Mapper<Price, PriceDto> {

    @Override
    public PriceDto mapEntityToDto(Price entity) {

        return PriceDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .value(entity.getValue())
                .date(entity.getDate())
                .subjectId(entity.getSubject().getId())
                .subjectName(entity.getSubject().getName())
                .build();
    }

    @Override
    public Price mapDtoToEntityForCreate(PriceDto dto) {
        Price entity = new Price();
        entity.setName(dto.getName());
        entity.setValue(dto.getValue());
        entity.setDate(dto.getDate());
        entity.setSubject(Subject.builder().id(dto.getSubjectId()).build());
        return entity;
    }

    @Override
    public Price mapDtoToEntityForUpdate(PriceDto dto, Price entity) {
        entity.setName(dto.getName());
        entity.setValue(dto.getValue());
        entity.setDate(dto.getDate());
        entity.setSubject(Subject.builder().id(dto.getSubjectId()).build());
        return entity;
    }

}
