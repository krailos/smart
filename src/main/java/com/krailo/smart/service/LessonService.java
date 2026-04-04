package com.krailo.smart.service;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.Student;
import com.krailo.smart.mapper.LessonMapper;
import com.krailo.smart.repository.LessonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class LessonService {

    private LessonRepository lessonRepository;
    private LessonMapper lessonMapper;

    public List<LessonDto> findAll() {
        return lessonRepository.findAll().stream()
                .peek(l -> l.getLessonsStudents()
                        .sort((o1, o2) -> o1.getStudent().getLastName().compareTo(o2.getStudent().getLastName())))
                .map(lessonMapper::mapEntityToDto).toList();
    }

   public  List<Lesson> findAllByStudentAndBetweenDatesEntity (Student student, LocalDate start, LocalDate end){
       return lessonRepository.findAllByStudentAndBetweenDates(student, start, end);
    }

    public LessonDto findById(Integer id) {
        Lesson lesson = lessonRepository.findById(id).get();
        lesson.getLessonsStudents()
                .sort((o1, o2) -> o1.getStudent().getLastName().compareTo(o2.getStudent().getLastName()));
        LessonDto lessonDto = lessonMapper.mapEntityToDto(lesson);
        return lessonDto;
//        return lessonRepository.findById(id).map(lessonMapper::mapEntityToDto)
//                .orElseThrow(() -> new EntityNotFoundException(String.format("Lessson whith id= %d not exist", id)));
    }

    public Lesson findByIdEntity(Integer id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Lessson whith id= %d not exist", id)));
    }

    public LessonDto create(LessonDto lessonDto) {
        return Optional.of(lessonDto).map(lessonMapper::mapDtoToEntityForCreate).map(lessonRepository::save)
                .map(lessonMapper::mapEntityToDto).orElseThrow();
    }

    public LessonDto update(Integer id, LessonDto lessonDto) {
        return lessonRepository.findById(id).map(entity -> lessonMapper.mapDtoToEntityForUpdate(lessonDto, entity))
                .map(lessonRepository::saveAndFlush).map(lessonMapper::mapEntityToDto)
                .orElseThrow(() -> new RuntimeException("smth wrong when update"));
    }

    public boolean delete(Integer id) {
        return lessonRepository.findById(id).map(entity -> {
            lessonRepository.delete(entity);
            return true;
        }).orElse(false);
    }
}