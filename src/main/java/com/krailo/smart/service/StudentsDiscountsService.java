package com.krailo.smart.service;

import com.krailo.smart.dto.StudentsDiscountsDto;
import com.krailo.smart.entity.StudentsDiscounts;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.mapper.StudentsDiscountsMapper;
import com.krailo.smart.repository.StudentsDiscountsRepository;
import com.krailo.smart.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@AllArgsConstructor
public class StudentsDiscountsService {

    private StudentsDiscountsRepository studentsDiscountsRepository;
    private StudentsDiscountsMapper studentsDiscountsMapper;

    // на уроці діє одна знижка курсу — найновіша за датою (нова замінює стару, не додається).
    // Дві з однаковою датою — незрозуміло, яка діє, тому заборонено
    private static final String SAME_DATE_MESSAGE =
            "На цю дату в учня вже є знижка на цей курс. Нова знижка замінює попередню — оберіть іншу дату";

    public List<StudentsDiscountsDto> findAll() {
        return studentsDiscountsRepository.findAll().stream().map(studentsDiscountsMapper::mapEntityToDto).toList();
    }

    public List<StudentsDiscountsDto> findAllByStudentId(Integer studentId) {
        return studentsDiscountsRepository.findAllByStudentId(studentId).stream().map(studentsDiscountsMapper::mapEntityToDto).toList();
    }

    public StudentsDiscountsDto findById(Integer id) {
        return studentsDiscountsRepository.findById(id).map(studentsDiscountsMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Знижка учня", id));
    }

    public StudentsDiscountsDto create(StudentsDiscountsDto studentsDiscountsDto) {
        // замість UNIQUE у БД: видалені знижки Hibernate не бачить (@SoftDelete),
        // тож повторно призначити знижку після видалення можна, а дві однакові активні — ні
        if (studentsDiscountsRepository.existsByStudentIdAndDiscountIdAndSubjectId(
                studentsDiscountsDto.getStudentId(), studentsDiscountsDto.getDiscountId(), studentsDiscountsDto.getSubjectId())) {
            throw new BusinessException("Учень уже має цю знижку на цей курс");
        }
        if (studentsDiscountsRepository.existsByStudentIdAndSubjectIdAndDate(
                studentsDiscountsDto.getStudentId(), studentsDiscountsDto.getSubjectId(), studentsDiscountsDto.getDate())) {
            throw new BusinessException(SAME_DATE_MESSAGE);
        }
        StudentsDiscounts studentsDiscounts = studentsDiscountsRepository.save(
                studentsDiscountsMapper.mapDtoToEntityForCreate(studentsDiscountsDto));
        return studentsDiscountsMapper.mapEntityToDto(studentsDiscounts);
    }

    public StudentsDiscountsDto update(Integer id, StudentsDiscountsDto studentsDiscountsDto) {
        // ті самі правила, що й при створенні, але без самого запису, який редагуємо
        if (studentsDiscountsRepository.existsByStudentIdAndDiscountIdAndSubjectIdAndIdNot(
                studentsDiscountsDto.getStudentId(), studentsDiscountsDto.getDiscountId(), studentsDiscountsDto.getSubjectId(), id)) {
            throw new BusinessException("Учень уже має цю знижку на цей курс");
        }
        if (studentsDiscountsRepository.existsByStudentIdAndSubjectIdAndDateAndIdNot(
                studentsDiscountsDto.getStudentId(), studentsDiscountsDto.getSubjectId(), studentsDiscountsDto.getDate(), id)) {
            throw new BusinessException(SAME_DATE_MESSAGE);
        }
        return studentsDiscountsRepository.findById(id)
                .map(entity -> studentsDiscountsMapper.mapDtoToEntityForUpdate(studentsDiscountsDto, entity))
                .map(studentsDiscountsRepository::saveAndFlush).map(studentsDiscountsMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Знижка учня", id));
    }

    public boolean delete(Integer id) {
        return studentsDiscountsRepository.findById(id).map(entity -> {
            studentsDiscountsRepository.delete(entity);
            return true;
        }).orElse(false);
    }

}
