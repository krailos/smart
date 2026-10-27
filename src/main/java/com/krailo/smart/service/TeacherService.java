package com.krailo.smart.service;

import com.krailo.smart.dto.TeacherDto;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.mapper.TeacherMapper;
import com.krailo.smart.repository.GangRepository;
import com.krailo.smart.repository.ScheduleRepository;
import com.krailo.smart.repository.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class TeacherService {

    private TeacherRepository teacherRepository;
    private TeacherMapper teacherMapper;
    private GangRepository gangRepository;
    private ScheduleRepository scheduleRepository;

    // тільки активні — для списку вчителів і всіх випадаючих меню у формах
    public List<TeacherDto> findAll() {
        return teacherRepository.findAllByArchived(false).stream().map(teacherMapper::mapEntityToDto).toList();
    }

    public List<TeacherDto> findAllArchived() {
        return teacherRepository.findAllByArchived(true).stream().map(teacherMapper::mapEntityToDto).toList();
    }

    // для сторінки /teachers, де показуємо кількість груп (випадаючим меню у формах групи не потрібні).
    // Проти N+1: вантажимо групи тут, у транзакції, — перший виклик тягне пачкою групи всіх вчителів.
    // Після транзакції (у шаблоні) пачками не вийде, і Hibernate робив би окремий запит на кожного вчителя
    public List<TeacherDto> findAllWithGangs(boolean archived) {
        List<Teacher> teachers = teacherRepository.findAllByArchived(archived);
        teachers.forEach(teacher -> Hibernate.initialize(teacher.getGangs()));
        return teachers.stream().map(teacher -> teacherMapper.mapEntityToDto(teacher, teacher.getGangs().size())).toList();
    }

    public TeacherDto findById(Integer id) {
        return teacherRepository.findById(id).map(teacherMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Teacher whith id= %d not exist", id)));
    }

    public Teacher findByIdEntity(Integer id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Teacher whith id= %d not exist", id)));
    }

    // Архівування замість видалення: вчитель зникає зі списків і форм,
    // але уроки (історія, зарплата) і далі бачать, хто їх провів.
    // Не можна архівувати, поки вчитель потрібен у майбутньому: веде активну групу або є в розкладі.
    public void archive(Integer id) {
        Teacher teacher = findByIdEntity(id);
        if (gangRepository.existsByTeacherIdAndArchivedFalse(id)) {
            throw new BusinessException("Не можна архівувати вчителя: він веде активну групу. "
                    + "Спершу змініть вчителя групи або архівуйте групу");
        }
        if (scheduleRepository.existsByTeacherId(id)) {
            throw new BusinessException("Не можна архівувати вчителя: він є в розкладі. "
                    + "Спершу змініть вчителя в розкладі або видаліть ці заняття з розкладу");
        }
        teacher.setArchived(true);
        teacherRepository.save(teacher);
    }

    public void restore(Integer id) {
        Teacher teacher = findByIdEntity(id);
        teacher.setArchived(false);
        teacherRepository.save(teacher);
    }

    public TeacherDto create(TeacherDto teacherDto) {
        return Optional.of(teacherDto).map(teacherMapper::mapDtoToEntityForCreate)
                .map(teacherRepository::save).map(teacherMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when create"));
    }

    public TeacherDto update(Integer id, TeacherDto teacherDto) {
        return teacherRepository.findById(id).map(entity -> teacherMapper.mapDtoToEntityForUpdate(teacherDto, entity))
                .map(teacherRepository::saveAndFlush).map(teacherMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }


}
