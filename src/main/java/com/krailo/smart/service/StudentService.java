package com.krailo.smart.service;

import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.entity.Student;
import com.krailo.smart.mapper.StudentMapper;
import com.krailo.smart.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class StudentService {
    
    private StudentRepository studentRepository;
    private StudentMapper studentMapper;

    
    public List<StudentDto> findAll() {
      return studentRepository.findAll().stream().sorted(Comparator.comparing(Student::getLastName)).map(studentMapper::mapEntityToDto).toList();
  }


    public StudentDto findById(Integer id) {
        return studentRepository.findById(id).map(studentMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("student whith id= %d not exist", id)));
    }
    
    public Student findByIdEntity(Integer id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("student whith id= %d not exist", id)));
    }


    public StudentDto create(StudentDto studentDto) {
        return Optional.of(studentDto).map(studentMapper::mapDtoToEntityForCreate).map(studentRepository::save)
                .map(studentMapper::mapEntityToDto).orElseThrow();
    }

    public StudentDto update(Integer id, StudentDto studentDto) {
        return studentRepository.findById(id)
                .map(entity -> studentMapper.mapDtoToEntityForUpdate(studentDto, entity))
                .map(studentRepository::saveAndFlush).map(studentMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    public boolean delete(Integer id) {
        return studentRepository.findById(id).map(entity -> {
            studentRepository.delete(entity);
            return true;
        }).orElse(false);
    }
}