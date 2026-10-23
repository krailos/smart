package com.krailo.smart.service;

import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.entity.Student;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.exception.ResourceNotFoundException;
import com.krailo.smart.mapper.StudentMapper;
import com.krailo.smart.repository.GangsStudentsRepository;
import com.krailo.smart.repository.StudentRepository;
import lombok.AllArgsConstructor;
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