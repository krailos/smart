package com.krailo.smart.service;

import com.krailo.smart.dto.GangDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.mapper.GangMapper;
import com.krailo.smart.repository.GangRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class GangService {

    private GangRepository gangRepository;
    private GangMapper gangMapper;

    
    public List<GangDto> findAll() {
      return gangRepository.findAll().stream().map(gangMapper::mapEntityToDto).toList();
  }

    public List<GangDto> findAllByTeacher(Teacher teacher) {
        return gangRepository.findAllByTeacher(teacher).stream().map(gangMapper::mapEntityToDto).toList();
    }


    public GangDto findById(Integer id) {
        return gangRepository.findById(id).map(gangMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Gang whith id= %d not exist", id)));
    }

    public Gang findByIdEntity(Integer id) {
        return gangRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Audience whith id= %d not exist", id)));
    }

    public GangDto create(GangDto gangDto) {
        return Optional.of(gangDto).map(gangMapper::mapDtoToEntityForCreate).map(gangRepository::save)
                .map(gangMapper::mapEntityToDto).orElseThrow();
    }


    public GangDto update(Integer id, GangDto gangDto) {
        return gangRepository.findById(id)
                .map(entity -> gangMapper.mapDtoToEntityForUpdate(gangDto, entity))
                .map(gangRepository::saveAndFlush).map(gangMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    public boolean delete(Integer id) {
        return gangRepository.findById(id).map(entity -> {
            gangRepository.delete(entity);
            return true;
        }).orElse(false);
    }
}
