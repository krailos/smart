package com.krailo.smart.mapper;

import com.krailo.smart.dto.PriceDto;
import com.krailo.smart.entity.Price;
import com.krailo.smart.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PriceMapper implements Mapper<Price, PriceDto> {

    SubjectRepository subjectRepository;

    @Override
    public PriceDto mapEntityToDto(Price entity) {

        return new PriceDto(
                entity.getId(),
                entity.getName(), 
                entity.getValue(),
                entity.getDate(),
                entity.getSubject().getId(), 
                entity.getSubject());
    }

    @Override
    public Price mapDtoToEntityForCreate(PriceDto dto) {
        Price entity = new Price();
        entity.setName(dto.getName());
        entity.setValue(dto.getValue());
        entity.setDate(dto.getDate());
        entity.setSubject(subjectRepository.findById(dto.getSubjectId()).orElseThrow(() -> new EntityNotFoundException(
                String.format("Subject whith id= %d not exist", dto.getSubjectId()))));
        return entity;
    }

    @Override
    public Price mapDtoToEntityForUpdate(PriceDto dto, Price entity) {
        entity.setName(dto.getName());
        entity.setValue(dto.getValue());
        entity.setDate(dto.getDate());
        entity.setSubject(subjectRepository.findById(dto.getSubjectId()).orElseThrow(() -> new EntityNotFoundException(
                String.format("Subject whith id= %d not exist", dto.getSubjectId()))));
        return entity;
    }

}
