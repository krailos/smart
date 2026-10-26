package com.krailo.smart.controller;

import com.krailo.smart.config.CurrentUser;
import com.krailo.smart.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Сторінка вчителя: учні груп, де він веде заняття (ім'я та контакт, без балансу й оплат).
// Учень з адреси не береться — тільки "свої", тож чужих тут побачити не можна
@Controller
@AllArgsConstructor
public class MyStudentsController {

    private final StudentService studentService;
    private final CurrentUser currentUser;

    @GetMapping("/my/students")
    public String myStudents(Model model) {
        Integer teacherId = currentUser.teacherId();
        model.addAttribute("noTeacher", teacherId == null);
        model.addAttribute("gangs", studentService.findGangsOfTeacher(teacherId));
        return "myStudents";
    }
}
