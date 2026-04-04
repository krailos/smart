package com.krailo.smart.service;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.Schedule;
import com.krailo.smart.mapper.ScheduleMapper;
import com.krailo.smart.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class ScheduleService {

    private ScheduleRepository scheduleRepository;
    private ScheduleMapper scheduleMapper;

    
    public List<ScheduleDto> findAll() {
      return scheduleRepository.findAll().stream().map(scheduleMapper::mapEntityToDto).toList();
  }


    public ScheduleDto findById(Integer id) {
        return scheduleRepository.findById(id).map(scheduleMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Gang whith id= %d not exist", id)));
    }

    public Schedule findByIdEntity(Integer id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Audience whith id= %d not exist", id)));
    }

    public ScheduleDto create(ScheduleDto scheduleDto) {
        return Optional.of(scheduleDto).map(scheduleMapper::mapDtoToEntityForCreate).map(scheduleRepository::save)
                .map(scheduleMapper::mapEntityToDto).orElseThrow();
    }

    public ScheduleDto update(Integer id, ScheduleDto scheduleDto) {
        return scheduleRepository.findById(id)
                .map(entity -> scheduleMapper.mapDtoToEntityForUpdate(scheduleDto, entity))
                .map(scheduleRepository::saveAndFlush).map(scheduleMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    public boolean delete(Integer id) {
        return scheduleRepository.findById(id).map(entity -> {
            scheduleRepository.delete(entity);
            return true;
        }).orElse(false);
    }
}
