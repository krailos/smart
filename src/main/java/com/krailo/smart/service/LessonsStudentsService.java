package com.krailo.smart.service;


import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.repository.LessonsStudentsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@AllArgsConstructor
public class LessonsStudentsService {

    LessonsStudentsRepository lessonsStudentsRepository;

    public LessonsStudents create (LessonsStudents lessonsStudents) {
        return lessonsStudentsRepository.save(lessonsStudents);
    }

}
