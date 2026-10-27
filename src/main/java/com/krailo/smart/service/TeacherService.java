package com.krailo.smart.service;

import com.krailo.smart.dto.TeacherDto;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.mapper.TeacherMapper;
import com.krailo.smart.repository.GangRepository;
import com.krailo.smart.repository.ScheduleRepository;
import com.krailo.smart.repository.TeacherRepository;
import com.krailo.smart.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    // Рахуємо тут, у транзакції: перший size() тягне пачкою групи всіх вчителів (проти N+1).
    // Після транзакції пачками не вийде — був би окремий запит на кожного вчителя
    public List<TeacherDto> findAllWithGangs(boolean archived) {
        List<Teacher> teachers = teacherRepository.findAllByArchived(archived);
        return teachers.stream().map(teacher -> teacherMapper.mapEntityToDto(teacher, teacher.getGangs().size())).toList();
    }

    public TeacherDto findById(Integer id) {
        return teacherRepository.findById(id).map(teacherMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Вчитель", id));
    }

    public Teacher findByIdEntity(Integer id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вчитель", id));
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
        Teacher teacher = teacherRepository.save(teacherMapper.mapDtoToEntityForCreate(teacherDto));
        return teacherMapper.mapEntityToDto(teacher);
    }

    public TeacherDto update(Integer id, TeacherDto teacherDto) {
        return teacherRepository.findById(id).map(entity -> teacherMapper.mapDtoToEntityForUpdate(teacherDto, entity))
                .map(teacherRepository::saveAndFlush).map(teacherMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Вчитель", id));
    }


}
