package com.krailo.smart.service;

import com.krailo.smart.dto.TeacherDto;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.mapper.TeacherMapper;
import com.krailo.smart.repository.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class TeacherService {

    private TeacherRepository teacherRepository;
    private TeacherMapper teacherMapper;

    public List<TeacherDto> findAll() {
        return teacherRepository.findAll().stream().map(teacherMapper::mapEntityToDto).toList();
    }

    public TeacherDto findById(Integer id) {
        return teacherRepository.findById(id).map(teacherMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Teacher whith id= %d not exist", id)));
    }

    public Teacher findByIdEntity(Integer id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Teacher whith id= %d not exist", id)));
    }

    public boolean delete(Integer id) {
        return teacherRepository.findById(id).map(entity -> {
            teacherRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    public TeacherDto create(TeacherDto teacherDto) {
        return Optional.of(teacherDto).map(teacherMapper::mapDtoToEntityForCreate)
                .map(teacherRepository::save).map(teacherMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when create"));
    }

    public TeacherDto update(Integer id, TeacherDto teacherDto) {
        return teacherRepository.findById(id).map(entity -> teacherMapper.mapDtoToEntityForUpdate(teacherDto, entity))
                .map(teacherRepository::saveAndFlush).map(teacherMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }


}
