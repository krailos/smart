package com.krailo.smart.controller;

import com.krailo.smart.dto.GangDto;
import com.krailo.smart.dto.GangsStudentsDto;
import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.entity.Gang;
import com.krailo.smart.entity.Student;
import com.krailo.smart.entity.Teacher;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.exception.ScheduleConflictException;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.StudentService;
import com.krailo.smart.service.SubjectService;
import com.krailo.smart.service.TeacherService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/gangs")
@AllArgsConstructor
public class GangController {

    private GangService gangService;
    private StudentService studentService;
    private SubjectService subjectService;
    private TeacherService teacherService;

    // /gangs — активні, /gangs?archived=true — архів (той самий шаблон)
    @GetMapping
    public String findAll(@RequestParam(defaultValue = "false") boolean archived, Model model) {
        model.addAttribute("gangs", archived ? gangService.findAllArchived() : gangService.findAll());
        model.addAttribute("archived", archived);
        return "gangs";
    }
    
    
    
    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
          model.addAttribute("gang", gangService.findById(id));
        model.addAttribute("subjects", subjectService.findAll());
        model.addAttribute("teachers", teacherService.findAll());
        return "gang";
    }
    
    
    @GetMapping("/{id}/bystudents")
    public String findByStudents (@PathVariable("id") Integer id, Model model) {
        GangDto gangDto =  gangService.findById(id);
        List<StudentDto> students = new ArrayList<StudentDto>();
        for (GangsStudentsDto gs : gangDto.getGangsStudentsDto()) {
            students.add(studentService.findById(gs.getStudentId()));
        }
        // для форми "Додати учня": активні учні, яких ще немає в групі
        List<Integer> inGang = students.stream().map(StudentDto::getId).toList();
        List<StudentDto> candidates = studentService.findAll().stream()
                .filter(s -> !inGang.contains(s.getId()))
                .toList();
          model.addAttribute("gang", gangDto);
          model.addAttribute("students", students);
          model.addAttribute("candidates", candidates);
        return "gangEditStudents";
    }

    @PostMapping("/{id}/students/add")
    // required = false: без вибраного учня (форму надіслали в обхід браузера) — повідомлення від сервісу, а не 500
    public String addStudent(@PathVariable("id") Integer id, @RequestParam(value = "studentId", required = false) Integer studentId,
                             RedirectAttributes redirectAttributes) {
        try {
            gangService.addStudent(id, studentId);
            redirectAttributes.addFlashAttribute("successMessage", "Учня додано в групу");
        } catch (BusinessException | ScheduleConflictException e) {
            // в архіві, уже в групі, зайнятий в цей час в іншій групі
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/gangs/" + id + "/bystudents";
    }

    @PostMapping("/{id}/students/{studentId}/remove")
    public String removeStudent(@PathVariable("id") Integer id, @PathVariable("studentId") Integer studentId,
                                RedirectAttributes redirectAttributes) {
        try {
            gangService.removeStudent(id, studentId);
            redirectAttributes.addFlashAttribute("successMessage", "Учня прибрано з групи");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/gangs/" + id + "/bystudents";
    }

    @GetMapping("/{id}/byTeacher")
    public String findAllByTeacher(@PathVariable("id") Integer teacherId, Model model){
        Teacher teacher = teacherService.findByIdEntity(teacherId);
        List<GangDto> gangs = gangService.findAllByTeacher(teacher);
        model.addAttribute("gangs", gangs);
        return "gangs";
    }

    @GetMapping("/{id}/byStudent")
    public String findAllByStudent(@PathVariable("id") Integer studentId, Model model){
        Student student = studentService.findByIdEntity(studentId);
        // у шаблон — GangDto, як і в інших методах (gangs.html читає subjectNme, teacherLastName)
        List<GangDto> gangs = student.getGangsStudents().stream()
                        .map(gs -> gangService.findById(gs.getGang().getId()))
                        .toList();
        model.addAttribute("gangs", gangs);
        return "gangs";
    }


    
    @GetMapping("/gang/new")
    public String createForm (Model model) {
    model.addAttribute("gang", new Gang());
    model.addAttribute("subjects", subjectService.findAll());
    model.addAttribute("teachers", teacherService.findAll());
    return "gangNew";
    }
    
    
    @PostMapping("/create")
   // @ResponseStatus(HttpStatus.CREATED)
    public String create ( @ModelAttribute GangDto gang) {
        gangService.create(gang);  
        return "redirect:/gangs";     
    }
    
    @PostMapping("/{id}/update")
    public String update (@PathVariable("id") Integer id, @ModelAttribute GangDto gangDto) {
           gangService.update(id, gangDto); 
        return "redirect:/gangs";        
    }
    
    // архівування замість видалення (див. GangService.archive)
    @PostMapping("/{id}/archive")
    public String archive (@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            gangService.archive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Групу переміщено в архів");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/gangs";
    }

    @PostMapping("/{id}/restore")
    public String restore (@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        gangService.restore(id);
        redirectAttributes.addFlashAttribute("successMessage", "Групу відновлено з архіву");
        return "redirect:/gangs?archived=true";
    }

}
