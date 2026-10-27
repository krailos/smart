package com.krailo.smart.service;

import com.krailo.smart.dto.PriceDto;
import com.krailo.smart.dto.SubjectDto;
import com.krailo.smart.entity.Price;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.mapper.PriceMapper;
import com.krailo.smart.repository.PriceRepository;
import com.krailo.smart.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Transactional
@Service
@AllArgsConstructor
public class PriceService {

    private PriceRepository priceRepository;
    private SubjectService subjectService;
    private PriceMapper priceMapper;

    public List<PriceDto> findAll() {
        return priceRepository.findAll().stream().map(priceMapper::mapEntityToDto).toList();
    }

    public PriceDto findById(Integer id) {
        return priceRepository.findById(id).map(priceMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Ціна", id));
    }

    public List<PriceDto> findBySubject(SubjectDto subjectDto) {
      return priceRepository.findBySubject(subjectService.findByIdEntity(subjectDto.getId())).stream().map(priceMapper::mapEntityToDto).toList();
    }

    public PriceDto create(PriceDto priceDto) {
        checkValue(priceDto.getValue());
        // замість UNIQUE (subject_id, price_date) у БД: видалені ціни Hibernate не бачить (@SoftDelete),
        // тож повторно додати ціну на ту саму дату після видалення можна, а дві активні — ні
        if (priceRepository.existsBySubjectIdAndDate(priceDto.getSubjectId(), priceDto.getDate())) {
            throw new BusinessException(String.format("Ціна цього курсу на %s уже є", priceDto.getDate()));
        }
        Price price = priceRepository.save(priceMapper.mapDtoToEntityForCreate(priceDto));
        return priceMapper.mapEntityToDto(price);
    }

    public PriceDto update(Integer id, PriceDto priceDto) {
        checkValue(priceDto.getValue());
        return priceRepository.findById(id)
                .map(entity -> priceMapper.mapDtoToEntityForUpdate(priceDto, entity))
                .map(priceRepository::saveAndFlush).map(priceMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Ціна", id));
    }

    // браузер теж перевіряє (required, min, step), але форму можна надіслати в обхід нього
    private void checkValue(BigDecimal value) {
        if (value == null) {
            throw new BusinessException("Вкажіть вартість уроку");
        }
        if (value.signum() < 0) {
            throw new BusinessException("Вартість уроку не може бути від'ємною");
        }
        // stripTrailingZeros: 100.500 -> 100.5, щоб зайві нулі не вважались копійками
        if (value.stripTrailingZeros().scale() > 2) {
            throw new BusinessException("Вартість уроку — не більше двох знаків після коми");
        }
    }

    public boolean delete(Integer id) {
        return priceRepository.findById(id).map(entity -> {
            priceRepository.delete(entity);
            return true;
        }).orElse(false);
    }
}
