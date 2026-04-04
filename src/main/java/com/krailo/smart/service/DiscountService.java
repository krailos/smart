package com.krailo.smart.service;

import com.krailo.smart.dto.DiscountDto;
import com.krailo.smart.mapper.DiscountMapper;
import com.krailo.smart.repository.DiscountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class DiscountService {

    private DiscountRepository discountRepository;
    private DiscountMapper discountMapper;

    public List<DiscountDto> findAll() {
        return discountRepository.findAll().stream().map(discountMapper::mapEntityToDto).toList();
    }

    public DiscountDto findById(Integer id) {
        return discountRepository.findById(id).map(discountMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Audience whith id= %d not exist", id)));
    }

    public DiscountDto create(DiscountDto discountDto) {
        return Optional.of(discountDto).map(discountMapper::mapDtoToEntityForCreate).map(discountRepository::save)
                .map(discountMapper::mapEntityToDto).orElseThrow();
    }

    public DiscountDto update(Integer id, DiscountDto discountDto) {
        return discountRepository.findById(id)
                .map(entity -> discountMapper.mapDtoToEntityForUpdate(discountDto, entity))
                .map(discountRepository::saveAndFlush).map(discountMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    public boolean delete(Integer id) {
        return discountRepository.findById(id).map(entity -> {
            discountRepository.delete(entity);
            return true;
        }).orElse(false);
    }

}
