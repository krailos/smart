package com.krailo.smart.controller;

import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.entity.Student;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/students")
@AllArgsConstructor
public class StudentController {

    private StudentService studentService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "students";
    }


    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("student", studentService.findById(id));
        model.addAttribute("gender", Gender.values());
        model.addAttribute("status", StudentStatus.values());
        return "student";
    }


    @GetMapping("/student/new")
    public String createForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("gender", Gender.values());
        model.addAttribute("status", StudentStatus.values());
        return "studentNew";
    }


    @PostMapping("/create")
    // @ResponseStatus(HttpStatus.CREATED)
    public String create(@ModelAttribute StudentDto student) {
        studentService.create(student);
        return "redirect:/students";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Integer id, @ModelAttribute StudentDto studentDto) {
        studentService.update(id, studentDto);
        return "redirect:/students";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Integer id) {
        studentService.delete(id);
        return "redirect:/students";
    }

}
