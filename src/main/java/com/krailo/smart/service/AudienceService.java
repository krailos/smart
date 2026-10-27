package com.krailo.smart.service;


import com.krailo.smart.dto.AudienceDto;
import com.krailo.smart.entity.Audience;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.mapper.AudienceMapper;
import com.krailo.smart.repository.AudienceRepository;
import com.krailo.smart.repository.ScheduleRepository;
import com.krailo.smart.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class AudienceService {

    private AudienceRepository audienceRepository;
    private AudienceMapper audienceMapper;
    private ScheduleRepository scheduleRepository;

    // тільки активні — для списку аудиторій і всіх випадаючих меню у формах
    public List<AudienceDto> findAll() {
        return audienceRepository.findAllByArchived(false).stream().map(audienceMapper::mapEntityToDto).toList();
    }

    public List<AudienceDto> findAllArchived() {
        return audienceRepository.findAllByArchived(true).stream().map(audienceMapper::mapEntityToDto).toList();
    }

    public AudienceDto findById(Integer id) {
        return audienceRepository.findById(id).map(audienceMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Аудиторія", id));
    }

    public Audience findByIdEntity(Integer id) {
        return audienceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Аудиторія", id));
    }

    public AudienceDto create(AudienceDto audienceDto) {
        return Optional.of(audienceDto).map(audienceMapper::mapDtoToEntityForCreate).map(audienceRepository::save)
                .map(audienceMapper::mapEntityToDto).orElseThrow();
    }

    public AudienceDto update(Integer id, AudienceDto audienceDto) {
        return audienceRepository.findById(id)
                .map(entity -> audienceMapper.mapDtoToEntityForUpdate(audienceDto, entity))
                .map(audienceRepository::saveAndFlush).map(audienceMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Аудиторія", id));
    }

    // Архівування замість видалення: аудиторія зникає зі списків і форм, але проведені уроки її пам'ятають.
    // Не можна архівувати, поки аудиторія є в розкладі.
    public void archive(Integer id) {
        Audience audience = findByIdEntity(id);
        if (scheduleRepository.existsByAudienceId(id)) {
            throw new BusinessException("Не можна архівувати аудиторію: вона є в розкладі. "
                    + "Спершу перенесіть ці заняття в іншу аудиторію або видаліть їх з розкладу");
        }
        audience.setArchived(true);
        audienceRepository.save(audience);
    }

    public void restore(Integer id) {
        Audience audience = findByIdEntity(id);
        audience.setArchived(false);
        audienceRepository.save(audience);
    }

}