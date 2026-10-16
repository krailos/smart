package com.krailo.smart.mapper;

import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.entity.Student;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LessonsStudentsMapper implements Mapper<LessonsStudents, LessonsStudentsDto>{

    @Override
    public LessonsStudents mapDtoToEntityForCreate(LessonsStudentsDto dto) {
        return LessonsStudents.builder()
                .lesson(Lesson.builder().id(dto.getLessonId()).build())
                .student(Student.builder().id(dto.getStudentId()).build())
                .present(dto.isPresent()).payed(dto.isPayed()).build();
    }

    @Override
    public LessonsStudentsDto mapEntityToDto(LessonsStudents e) {
        return LessonsStudentsDto.builder()
                .id(e.getId())
                .lessonId(e.getLesson().getId())
                .studentLastName(e.getStudent().getLastName())
                .studentFirstName(e.getStudent().getFirstName())
                .present(e.isPresent())
                .payed(e.isPayed()).build();
    }

    @Override
    public LessonsStudents mapDtoToEntityForUpdate(LessonsStudentsDto dto, LessonsStudents e) {
         e.setLesson(Lesson.builder().id(dto.getLessonId()).build());
         e.setStudent(Student.builder().id(dto.getStudentId()).build());
         e.setPresent(dto.isPresent());
         e.setPayed(dto.isPayed());
        return e;
    }
}
