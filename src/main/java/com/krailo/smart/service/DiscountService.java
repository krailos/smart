package com.krailo.smart.service;

import com.krailo.smart.dto.DiscountDto;
import com.krailo.smart.entity.Discount;
import com.krailo.smart.mapper.DiscountMapper;
import com.krailo.smart.repository.DiscountRepository;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
                .orElseThrow(() -> new NotFoundException("Знижка", id));
    }

    public DiscountDto create(DiscountDto discountDto) {
        checkUnique(discountDto, 0);
        Discount discount = discountRepository.save(discountMapper.mapDtoToEntityForCreate(discountDto));
        return discountMapper.mapEntityToDto(discount);
    }

    public DiscountDto update(Integer id, DiscountDto discountDto) {
        checkUnique(discountDto, id);
        return discountRepository.findById(id)
                .map(entity -> discountMapper.mapDtoToEntityForUpdate(discountDto, entity))
                .map(discountRepository::saveAndFlush).map(discountMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Знижка", id));
    }

    // У базі UNIQUE (discount_date, name): без цієї перевірки дубль давав би 500 замість зрозумілого тексту.
    // excludeId — id знижки, яку редагуємо (для нової = 0)
    private void checkUnique(DiscountDto d, int excludeId) {
        if (discountRepository.existsByNameAndDateAndIdNot(d.getName(), d.getDate(), excludeId)) {
            throw new BusinessException(String.format("Знижка «%s» з датою %s уже є", d.getName(),
                    d.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        }
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
                .orElseThrow(() -> new NotFoundException("Знижка", id));
    }

}
