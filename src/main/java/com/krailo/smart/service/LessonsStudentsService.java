package com.krailo.smart.service;


import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.mapper.LessonsStudentsMapper;
import com.krailo.smart.repository.LessonsStudentsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class LessonsStudentsService {

    LessonsStudentsRepository lessonsStudentsRepository;
    LessonsStudentsMapper lessonsStudentsMapper;

    public LessonsStudentsDto create(LessonsStudentsDto lessonsStudentsDto) {
        return Optional.of(lessonsStudentsDto)
                .map(lessonsStudentsMapper::mapDtoToEntityForCreate)
                .map(lessonsStudentsRepository::saveAndFlush)
                .map(lessonsStudentsMapper::mapEntityToDto)
                .orElseThrow();
    }


    public List<LessonsStudentsDto> findAll (){
        return lessonsStudentsRepository.findAll().stream()
                .map(lessonsStudentsMapper::mapEntityToDto).toList();
    }



}
