package com.krailo.smart.service;


import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.mapper.LessonsStudentsMapper;
import com.krailo.smart.repository.LessonsStudentsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@AllArgsConstructor
public class LessonsStudentsService {

    LessonsStudentsRepository lessonsStudentsRepository;
    LessonsStudentsMapper lessonsStudentsMapper;

    public LessonsStudentsDto create(LessonsStudentsDto lessonsStudentsDto) {
        LessonsStudents lessonsStudents = lessonsStudentsRepository.saveAndFlush(
                lessonsStudentsMapper.mapDtoToEntityForCreate(lessonsStudentsDto));
        return lessonsStudentsMapper.mapEntityToDto(lessonsStudents);
    }



}
