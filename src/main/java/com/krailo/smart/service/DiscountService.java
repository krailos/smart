package com.krailo.smart.service;

import com.krailo.smart.dto.DiscountDto;
import com.krailo.smart.entity.Discount;
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

    // тільки активні — для списку знижок і випадаючого меню "знижка учня"
    public List<DiscountDto> findAll() {
        return discountRepository.findAllByArchived(false).stream().map(discountMapper::mapEntityToDto).toList();
    }

    public List<DiscountDto> findAllArchived() {
        return discountRepository.findAllByArchived(true).stream().map(discountMapper::mapEntityToDto).toList();
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

    // Архівування замість видалення: знижку більше не можна призначити новим учням,
    // але вже призначені знижки й розрахунки за минулі уроки лишаються як були.
    public void archive(Integer id) {
        Discount discount = findByIdEntity(id);
        discount.setArchived(true);
        discountRepository.save(discount);
    }

    public void restore(Integer id) {
        Discount discount = findByIdEntity(id);
        discount.setArchived(false);
        discountRepository.save(discount);
    }

    private Discount findByIdEntity(Integer id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Discount whith id= %d not exist", id)));
    }

}
