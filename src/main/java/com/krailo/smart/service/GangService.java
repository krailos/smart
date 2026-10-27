package com.krailo.smart.service;

import com.krailo.smart.dto.GangDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.GangsStudents;
import com.krailo.smart.entity.Student;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.mapper.GangMapper;
import com.krailo.smart.repository.GangRepository;
import com.krailo.smart.repository.GangsStudentsRepository;
import com.krailo.smart.repository.ScheduleRepository;
import com.krailo.smart.repository.StudentRepository;
import com.krailo.smart.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@AllArgsConstructor
public class GangService {

    private GangRepository gangRepository;
    private GangMapper gangMapper;
    private ScheduleRepository scheduleRepository;
    private GangsStudentsRepository gangsStudentsRepository;
    private StudentRepository studentRepository;
    private ScheduleService scheduleService;

    // тільки активні, без учнів — для випадаючих меню у формах
    public List<GangDto> findAll() {
      return gangRepository.findAllByArchived(false).stream().map(gangMapper::mapEntityToDto).toList();
  }

    public List<GangDto> findAllArchived() {
        return gangRepository.findAllByArchived(true).stream().map(gangMapper::mapEntityToDto).toList();
    }

    // списки груп (gangs.html показує кількість учнів). Учні мапляться тут, у транзакції:
    // перше звернення тягне пачкою учнів усіх груп (проти N+1)
    public List<GangDto> findAllWithStudents(boolean archived) {
        return gangRepository.findAllByArchived(archived).stream().map(gangMapper::mapEntityToDtoWithStudents).toList();
    }

    public List<GangDto> findAllByTeacher(Teacher teacher) {
        return gangRepository.findAllByTeacher(teacher).stream().map(gangMapper::mapEntityToDtoWithStudents).toList();
    }

    // групи учня (/students/{id}/gangs)
    public List<GangDto> findAllByStudent(Integer studentId) {
        return gangsStudentsRepository.findAllByStudentId(studentId).stream()
                .map(GangsStudents::getGang)
                .map(gangMapper::mapEntityToDtoWithStudents).toList();
    }

    // з учнями — картка групи, «Учні групи», проведення уроку
    public GangDto findById(Integer id) {
        return gangRepository.findById(id).map(gangMapper::mapEntityToDtoWithStudents)
                .orElseThrow(() -> new NotFoundException("Група", id));
    }

    public Gang findByIdEntity(Integer id) {
        return gangRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Група", id));
    }

    public GangDto create(GangDto gangDto) {
        Gang gang = gangRepository.save(gangMapper.mapDtoToEntityForCreate(gangDto));
        return gangMapper.mapEntityToDto(gang);
    }


    public GangDto update(Integer id, GangDto gangDto) {
        return gangRepository.findById(id)
                .map(entity -> gangMapper.mapDtoToEntityForUpdate(gangDto, entity))
                .map(gangRepository::saveAndFlush).map(gangMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Група", id));
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

    // Додати учня в групу. Склад групи впливає тільки на НОВІ уроки — проведені не змінюються.
    public void addStudent(Integer gangId, Integer studentId) {
        if (studentId == null) {
            throw new BusinessException("Оберіть учня");
        }
        Gang gang = findByIdEntity(gangId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Учень", studentId));
        if (gang.isArchived()) {
            throw new BusinessException("Група в архіві — додавати учнів не можна");
        }
        if (student.isArchived()) {
            throw new BusinessException(student.getLastName() + " в архіві — спершу відновіть учня");
        }
        if (gangsStudentsRepository.existsByGangIdAndStudentId(gangId, studentId)) {
            throw new BusinessException(student.getLastName() + " уже в цій групі");
        }
        // учень не може бути на двох заняттях одночасно (кидає ScheduleConflictException)
        scheduleService.checkStudentFree(student, gang);
        gangsStudentsRepository.save(GangsStudents.builder().gang(gang).student(student).build());
    }

    // Прибрати учня з групи: м'яке видалення (@SoftDelete) — проведені уроки, оплати, знижки не змінюються
    public void removeStudent(Integer gangId, Integer studentId) {
        GangsStudents gangStudent = gangsStudentsRepository.findByGangIdAndStudentId(gangId, studentId)
                .orElseThrow(() -> new BusinessException("Цього учня вже немає в групі"));
        gangsStudentsRepository.delete(gangStudent);
    }
}
