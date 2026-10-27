package com.krailo.smart.controller;

import com.krailo.smart.dto.TeacherDto;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.SubjectService;
import com.krailo.smart.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final SubjectService subjectService;
    private final TeacherService teacherService;
    private final GangService gangService;

    // /teachers — активні, /teachers?archived=true — архів (той самий шаблон)
    @GetMapping
    public String findAll(@RequestParam(defaultValue = "false") boolean archived, Model model) {
        model.addAttribute("teachers", teacherService.findAllWithGangs(archived));
        model.addAttribute("archived", archived);
        return "teachers";
    }
    
    
    
    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
          model.addAttribute("teacher", teacherService.findById(id));
        return "teacher";
    }

    // групи вчителя (була /gangs/{id}/byTeacher — там id був вчителя, а не групи)
    @GetMapping("/{id}/gangs")
    public String gangs(@PathVariable("id") Integer id, Model model) {
        Teacher teacher = teacherService.findByIdEntity(id);
        model.addAttribute("gangs", gangService.findAllByTeacher(teacher));
        return "gangs";
    }
    
    
    @GetMapping("/new")
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
    
    // архівування замість видалення (див. TeacherService.archive)
    @PostMapping("/{id}/archive")
    public String archive (@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.archive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Вчителя переміщено в архів");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teachers";
    }

    @PostMapping("/{id}/restore")
    public String restore (@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        teacherService.restore(id);
        redirectAttributes.addFlashAttribute("successMessage", "Вчителя відновлено з архіву");
        return "redirect:/teachers?archived=true";
    }

}
