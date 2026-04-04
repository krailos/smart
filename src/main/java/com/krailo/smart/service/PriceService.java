package com.krailo.smart.service;

import com.krailo.smart.dto.PriceDto;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.mapper.PriceMapper;
import com.krailo.smart.repository.PriceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class PriceService {

    private PriceRepository priceRepository;
    private PriceMapper priceMapper;

    public List<PriceDto> findAll() {
        return priceRepository.findAll().stream().map(priceMapper::mapEntityToDto).toList();
    }

    public PriceDto findById(Integer id) {
        return priceRepository.findById(id).map(priceMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Price whith id= %d not exist", id)));
    }

    public List<PriceDto> findBySubject(Subject subject) {
      return priceRepository.findBySubject(subject).stream().map(priceMapper::mapEntityToDto).toList();
    }

    public PriceDto create(PriceDto priceDto) {
        return Optional.of(priceDto).map(priceMapper::mapDtoToEntityForCreate).map(priceRepository::save)
                .map(priceMapper::mapEntityToDto).orElseThrow();
    }

    public PriceDto update(Integer id, PriceDto priceDto) {
        return priceRepository.findById(id)
                .map(entity -> priceMapper.mapDtoToEntityForUpdate(priceDto, entity))
                .map(priceRepository::saveAndFlush).map(priceMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    public boolean delete(Integer id) {
        return priceRepository.findById(id).map(entity -> {
            priceRepository.delete(entity);
            return true;
        }).orElse(false);
    }
}
