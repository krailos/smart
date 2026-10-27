package com.krailo.smart.service;

import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.dto.TeacherGangDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.GangsStudents;
import com.krailo.smart.entity.Schedule;
import com.krailo.smart.entity.Student;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.exception.ResourceNotFoundException;
import com.krailo.smart.mapper.StudentMapper;
import com.krailo.smart.repository.GangsStudentsRepository;
import com.krailo.smart.repository.ScheduleRepository;
import com.krailo.smart.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class StudentService {
    
    private StudentRepository studentRepository;
    private StudentMapper studentMapper;
    private GangsStudentsRepository gangsStudentsRepository;
    private ScheduleRepository scheduleRepository;

    // "Мої учні" вчителя: групи, де він веде заняття, і їхні учні (активні), за прізвищем.
    // Учнів, прибраних з групи, Hibernate не бачить сам (@SoftDelete на GangsStudents)
    @Transactional(readOnly = true)
    public List<TeacherGangDto> findGangsOfTeacher(Integer teacherId) {
        if (teacherId == null) {
            return List.of();
        }
        return scheduleRepository.findAllByTeacherId(teacherId).stream()
                .map(Schedule::getGang)
                .filter(gang -> !gang.isArchived())
                .distinct()
                .sorted(Comparator.comparing(Gang::getName))
                .map(gang -> new TeacherGangDto(
                        gang.getName(),
                        gang.getSubject() == null ? null : gang.getSubject().getName(),
                        gang.getGangStudents().stream()
                                .map(GangsStudents::getStudent)
                                .filter(student -> !student.isArchived())
                                .sorted(Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName))
                                .map(student -> new TeacherGangDto.Student(student.getLastName(), student.getFirstName(),
                                        student.getContactName(), student.getPhone()))
                                .toList()))
                .toList();
    }


    // archived = false — список активних учнів, true — архів
    public Page<StudentDto> findAllByPage(int page, int size, String sortBy, String sortDir, String keyword, boolean archived){
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        // Перевіряємо наявність ключового слова
        Page<Student> studentPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            studentPage = studentRepository.searchStudents(keyword.trim(), archived, pageable);
        } else {
            studentPage = studentRepository.findAllByArchived(archived, pageable);
        }
        // проти N+1: у списку показуємо кількість груп і знижок учня. Вантажимо їх тут, у транзакції, —
        // перший виклик тягне пачкою списки всіх учнів сторінки. Після транзакції (у шаблоні) пачками не вийде,
        // і Hibernate робив би окремий запит на кожного учня
        studentPage.forEach(student -> {
            Hibernate.initialize(student.getGangsStudents());
            Hibernate.initialize(student.getStudentsDiscounts());
        });
        // Мапимо сутності в DTO
        return studentPage.map(student -> studentMapper.mapEntityToDto(student));
    }

    // тільки активні — для випадаючих меню у формах (оплата, знижка учня)
    public List<StudentDto> findAll() {
      return studentRepository.findAllByArchived(false).stream().sorted(Comparator.comparing(Student::getLastName)).map(studentMapper::mapEntityToDto).toList();
  }

    // активні й архівні — для звітів: учень в архіві теж може мати борг
    public List<StudentDto> findAllIncludingArchived() {
        return studentRepository.findAll().stream().sorted(Comparator.comparing(Student::getLastName)).map(studentMapper::mapEntityToDto).toList();
    }


    public StudentDto findById(Integer id) {
        return studentRepository.findById(id).map(studentMapper::mapEntityToDto)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("student whith id= %d not exist", id)));
    }
    
    public Student findByIdEntity(Integer id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("student whith id= %d not exist", id)));
    }


    public StudentDto create(StudentDto studentDto) {
        return Optional.of(studentDto).map(studentMapper::mapDtoToEntityForCreate).map(studentRepository::save)
                .map(studentMapper::mapEntityToDto).orElseThrow();
    }

    public StudentDto update(Integer id, StudentDto studentDto) {
        return studentRepository.findById(id)
                .map(entity -> studentMapper.mapDtoToEntityForUpdate(studentDto, entity))
                .map(studentRepository::saveAndFlush).map(studentMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    // Архівування замість видалення: учень зникає зі списків і форм,
    // але його уроки, оплати, журнал транзакцій і баланс лишаються (звіт по ньому можна зробити й з архіву).
    // Не можна архівувати, поки учень у активній групі — інакше він з'являтиметься у формі проведення уроку.
    public void archive(Integer id) {
        Student student = findByIdEntity(id);
        if (gangsStudentsRepository.existsByStudentIdAndGangArchivedFalse(id)) {
            throw new BusinessException("Не можна архівувати учня: він ще в активній групі. Спершу приберіть його з групи");
        }
        student.setArchived(true);
        studentRepository.save(student);
    }

    public void restore(Integer id) {
        Student student = findByIdEntity(id);
        student.setArchived(false);
        studentRepository.save(student);
    }
}