package com.krailo.smart.controller;

import com.krailo.smart.repository.LessonsStudentsRepository;
import com.krailo.smart.service.LessonsStudentsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class LessonsStudentsController {

    private LessonsStudentsService lessonsStudentsService;
    private LessonsStudentsRepository lessonsStudentsRepository;

    @GetMapping("/lesson-students")
    String findAll(Model model) {
        model.addAttribute("lessonsStudents", lessonsStudentsService.findAll());
        return "lessonsStudents";
    }
}
