package com.krailo.smart.mapper;

import com.krailo.smart.dto.SubjectDto;
import com.krailo.smart.entity.Subject;
import org.springframework.stereotype.Component;


@Component
public class SubjectMapper implements Mapper<Subject, SubjectDto> {
    private PriceMapper priceMapper;
    private GangMapper gangMapper;

    @Override
    public SubjectDto mapEntityToDto(Subject entity) {

        return SubjectDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .pricesDto(entity.getPrices().stream().map(priceMapper::mapEntityToDto).toList())
                .gangsDto(entity.getGangs().stream().map(gangMapper::mapEntityToDto).toList())
                .build();
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
