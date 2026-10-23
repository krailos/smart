package com.krailo.smart.service;

import com.krailo.smart.dto.GangDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.mapper.GangMapper;
import com.krailo.smart.repository.GangRepository;
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
public class GangService {

    private GangRepository gangRepository;
    private GangMapper gangMapper;
    private ScheduleRepository scheduleRepository;

    // тільки активні — для списку груп і всіх випадаючих меню у формах
    public List<GangDto> findAll() {
      return gangRepository.findAllByArchived(false).stream().map(gangMapper::mapEntityToDto).toList();
  }

    public List<GangDto> findAllArchived() {
        return gangRepository.findAllByArchived(true).stream().map(gangMapper::mapEntityToDto).toList();
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

    // Архівування замість видалення: група зникає зі списків і форм, але проведені уроки її пам'ятають.
    // Не можна архівувати, поки група є в розкладі.
    public void archive(Integer id) {
        Gang gang = findByIdEntity(id);
        if (scheduleRepository.existsByGangId(id)) {
            throw new BusinessException("Не можна архівувати групу: вона є в розкладі. Спершу видаліть її заняття з розкладу");
        }
        gang.setArchived(true);
        gangRepository.save(gang);
    }

    public void restore(Integer id) {
        Gang gang = findByIdEntity(id);
        gang.setArchived(false);
        gangRepository.save(gang);
    }
}
