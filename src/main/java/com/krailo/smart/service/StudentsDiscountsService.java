package com.krailo.smart.service;

import com.krailo.smart.dto.StudentsDiscountsDto;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.mapper.StudentsDiscountsMapper;
import com.krailo.smart.repository.StudentsDiscountsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class StudentsDiscountsService {

    private StudentsDiscountsRepository studentsDiscountsRepository;
    private StudentsDiscountsMapper studentsDiscountsMapper;

    public List<StudentsDiscountsDto> findAll() {
        return studentsDiscountsRepository.findAll().stream().map(studentsDiscountsMapper::mapEntityToDto).toList();
    }

    public List<StudentsDiscountsDto> findAllByStudentId(Integer studentId) {
        return studentsDiscountsRepository.findAllByStudentId(studentId).stream().map(studentsDiscountsMapper::mapEntityToDto).toList();
    }

    public StudentsDiscountsDto findById(Integer id) {
        return studentsDiscountsRepository.findById(id).map(studentsDiscountsMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("studentsDiscounts whith id= %d not exist", id)));
    }

    public StudentsDiscountsDto create(StudentsDiscountsDto studentsDiscountsDto) {
        // замість UNIQUE у БД: видалені знижки Hibernate не бачить (@SoftDelete),
        // тож повторно призначити знижку після видалення можна, а дві однакові активні — ні
        if (studentsDiscountsRepository.existsByStudentIdAndDiscountIdAndSubjectId(
                studentsDiscountsDto.getStudentId(), studentsDiscountsDto.getDiscountId(), studentsDiscountsDto.getSubjectId())) {
            throw new BusinessException("Учень уже має цю знижку на цей курс");
        }
        return Optional.of(studentsDiscountsDto).map(studentsDiscountsMapper::mapDtoToEntityForCreate).map(studentsDiscountsRepository::save)
                .map(studentsDiscountsMapper::mapEntityToDto).orElseThrow();
    }

    public StudentsDiscountsDto update(Integer id, StudentsDiscountsDto studentsDiscountsDto) {
        return studentsDiscountsRepository.findById(id)
                .map(entity -> studentsDiscountsMapper.mapDtoToEntityForUpdate(studentsDiscountsDto, entity))
                .map(studentsDiscountsRepository::saveAndFlush).map(studentsDiscountsMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    public boolean delete(Integer id) {
        return studentsDiscountsRepository.findById(id).map(entity -> {
            studentsDiscountsRepository.delete(entity);
            return true;
        }).orElse(false);
    }

}
