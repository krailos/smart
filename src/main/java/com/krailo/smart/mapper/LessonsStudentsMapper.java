package com.krailo.smart.mapper;

import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.service.LessonService;
import com.krailo.smart.service.StudentService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class LessonsStudentsMapper implements Mapper<LessonsStudents, LessonsStudentsDto>{
   private LessonService lessonService;
   private StudentService studentService;

    @Override
    public LessonsStudents mapDtoToEntityForCreate(LessonsStudentsDto dto) {
        return LessonsStudents.builder().lesson(lessonService.findByIdEntity(dto.getLessonId()))
                .student(studentService.findByIdEntity(dto.getStudentId()))
                .present(dto.isPresent()).payed(dto.isPayed()).build();
    }

    @Override
    public LessonsStudentsDto mapEntityToDto(LessonsStudents e) {
        return LessonsStudentsDto.builder().id(e.getId())
                .lessonId(e.getLesson().getId()).studentId(e.getStudent().getId())
                .present(e.isPresent()).payed(e.isPayed()).build();
    }

    @Override
    public LessonsStudents mapDtoToEntityForUpdate(LessonsStudentsDto dto, LessonsStudents e) {
         e.setLesson(lessonService.findByIdEntity(dto.getLessonId()));
         e.setStudent(studentService.findByIdEntity(dto.getStudentId()));
         e.setPresent(dto.isPresent());
         e.setPayed(dto.isPayed());
        return e;
    }
}
