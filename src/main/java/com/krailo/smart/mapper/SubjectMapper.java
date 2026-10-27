package com.krailo.smart.mapper;

import com.krailo.smart.dto.SubjectDto;
import com.krailo.smart.entity.Subject;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class SubjectMapper implements Mapper<Subject, SubjectDto> {

    PriceMapper priceMapper;

    // без цін (pricesDto = null) — для випадаючих меню: ціни курсу з бази не вантажаться
    @Override
    public SubjectDto mapEntityToDto(Subject entity) {
        return builder(entity).build();
    }

    // з цінами — список курсів, картка курсу, сторінка цін
    public SubjectDto mapEntityToDtoWithPrices(Subject entity) {
        return builder(entity)
                .pricesDto(entity.getPrices().stream().map(priceMapper::mapEntityToDto).toList())
                .build();
    }

    private SubjectDto.SubjectDtoBuilder builder(Subject entity) {
        return SubjectDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription());
    }

    @Override
    public Subject mapDtoToEntityForCreate(SubjectDto dto) {
        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
        return subject;
    }

    @Override
    public Subject mapDtoToEntityForUpdate(SubjectDto dto, Subject entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }

}
