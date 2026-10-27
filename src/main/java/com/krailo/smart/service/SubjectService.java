package com.krailo.smart.service;

import com.krailo.smart.dto.SubjectDto;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.mapper.SubjectMapper;
import com.krailo.smart.repository.GangRepository;
import com.krailo.smart.repository.ScheduleRepository;
import com.krailo.smart.repository.SubjectRepository;
import com.krailo.smart.exception.NotFoundException;
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

    // тільки активні, без цін — для випадаючих меню у формах
    public List<SubjectDto> findAll() {
      return subjectRepository.findAllByArchived(false).stream().map(subjectMapper::mapEntityToDto).toList();
  }

    public List<SubjectDto> findAllArchived() {
        return subjectRepository.findAllByArchived(true).stream().map(subjectMapper::mapEntityToDto).toList();
    }

    // для списку курсів (subjects.html показує першу ціну — найновішу). Ціни мапляться тут, у транзакції:
    // перше звернення тягне пачкою ціни всіх курсів (проти N+1)
    public List<SubjectDto> findAllWithPrices(boolean archived) {
        return subjectRepository.findAllByArchived(archived).stream()
                .peek(s -> s.getPrices().sort((o1, o2) -> o2.getDate().compareTo(o1.getDate())))
                .map(subjectMapper::mapEntityToDtoWithPrices).toList();
    }

    // з цінами — картка курсу і сторінка цін курсу
    public SubjectDto findById(Integer id) {
        return subjectRepository.findById(id).map(subjectMapper::mapEntityToDtoWithPrices)
                .orElseThrow(() -> new NotFoundException("Курс", id));
    }

    public Subject findByIdEntity(Integer id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Курс", id));
    }

    public SubjectDto create(SubjectDto subjectDto) {
        return Optional.of(subjectDto).map(subjectMapper::mapDtoToEntityForCreate).map(subjectRepository::save)
                .map(subjectMapper::mapEntityToDto).orElseThrow();
    }

    public SubjectDto update(Integer id, SubjectDto subjectDto) {
        return subjectRepository.findById(id)
                .map(entity -> subjectMapper.mapDtoToEntityForUpdate(subjectDto, entity))
                .map(subjectRepository::saveAndFlush).map(subjectMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Курс", id));
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
