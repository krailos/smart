package com.krailo.smart.service;

import com.krailo.smart.dto.SubjectDto;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.mapper.SubjectMapper;
import com.krailo.smart.repository.GangRepository;
import com.krailo.smart.repository.ScheduleRepository;
import com.krailo.smart.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class SubjectService {

    private SubjectRepository subjectRepository;
    private SubjectMapper subjectMapper;
    private GangRepository gangRepository;
    private ScheduleRepository scheduleRepository;

    // тільки активні — для списку курсів і всіх випадаючих меню у формах
    public List<SubjectDto> findAll() {
      return findAllByArchived(false);
  }

    public List<SubjectDto> findAllArchived() {
        return findAllByArchived(true);
    }

    private List<SubjectDto> findAllByArchived(boolean archived) {
        return subjectRepository.findAllByArchived(archived).stream()
                .peek(s -> s.getPrices().sort((o1, o2) -> o2.getDate().compareTo(o1.getDate())))
                .map(subjectMapper::mapEntityToDto).toList();
    }


    public SubjectDto findById(Integer id) {
        return subjectRepository.findById(id).map(subjectMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Subject whith id= %d not exist", id)));
    }

    public Subject findByIdEntity(Integer id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Audience whith id= %d not exist", id)));
    }

    public SubjectDto create(SubjectDto subjectDto) {
        return Optional.of(subjectDto).map(subjectMapper::mapDtoToEntityForCreate).map(subjectRepository::save)
                .map(subjectMapper::mapEntityToDto).orElseThrow();
    }

    public SubjectDto update(Integer id, SubjectDto subjectDto) {
        return subjectRepository.findById(id)
                .map(entity -> subjectMapper.mapDtoToEntityForUpdate(subjectDto, entity))
                .map(subjectRepository::saveAndFlush).map(subjectMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    // Архівування замість видалення: курс зникає зі списків і форм, але уроки, ціни й оплати в історії лишаються.
    // Не можна архівувати, поки курс потрібен у майбутньому: є активні групи або заняття в розкладі.
    public void archive(Integer id) {
        Subject subject = findByIdEntity(id);
        if (gangRepository.existsBySubjectIdAndArchivedFalse(id)) {
            throw new BusinessException("Не можна архівувати курс: є активні групи цього курсу. Спершу архівуйте групи");
        }
        if (scheduleRepository.existsBySubjectId(id)) {
            throw new BusinessException("Не можна архівувати курс: він є в розкладі. Спершу видаліть ці заняття з розкладу");
        }
        subject.setArchived(true);
        subjectRepository.save(subject);
    }

    public void restore(Integer id) {
        Subject subject = findByIdEntity(id);
        subject.setArchived(false);
        subjectRepository.save(subject);
    }
}
