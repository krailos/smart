package com.krailo.smart.controller;

import com.krailo.smart.dto.TeacherDto;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.service.SubjectService;
import com.krailo.smart.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final SubjectService subjectService;
    private final TeacherService teacherService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("teachers", teacherService.findAll());

        return "teachers";
    }
    
    
    
    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
          model.addAttribute("teacher", teacherService.findById(id));
        return "teacher";
    }
    
    
    @GetMapping("/teacher/new")
    public String createForm (Model model) {
    model.addAttribute("teacher", new Teacher());
    return "teacherNew";
    }
    
    
    @PostMapping("/create")
   // @ResponseStatus(HttpStatus.CREATED)
    public String create ( @ModelAttribute TeacherDto teacherDto) {
        teacherService.create(teacherDto);
        return "redirect:/teachers";
    }
    
    @PostMapping("/{id}/update")
    public String update (@PathVariable("id") Integer id, @ModelAttribute TeacherDto teacherDto) {
        teacherService.update(id, teacherDto);
        return "redirect:/teachers";
    }
    
    @PostMapping("/{id}/delete")
    public String delete (@PathVariable("id") Integer id) {
        teacherService.delete(id);
        return "redirect:/teachers";
    }

}
