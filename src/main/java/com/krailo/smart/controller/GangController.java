package com.krailo.smart.controller;

import com.krailo.smart.dto.GangDto;
import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.entity.GangsStudents;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/gangs")
@AllArgsConstructor
public class GangController {

    private GangService gangService;
    private StudentService studentService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("gangs", gangService.findAll());
        return "/gangs";
    }
    
    
    
    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
          model.addAttribute("gang", gangService.findById(id));
        return "/gang";        
    }
    
    
    @GetMapping("/{id}/bystudents")
    public String findByStudents (@PathVariable("id") Integer id, Model model) {
        GangDto gangDto =  gangService.findById(id);
        List<StudentDto> students = new ArrayList<StudentDto>();
        for (GangsStudents gs : gangDto.getGangStudents()) {
            students.add(studentService.findById(gs.getStudent().getId()));
        }
          model.addAttribute("gang", gangDto);
          model.addAttribute("students", students);
        return "/gangEditStudents";        
    }
    
    @GetMapping("/gang/new")
    public String createForm (Model model) {
    model.addAttribute("gang", new Subject());
    return "/gangNew";
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
    
    @PostMapping("/{id}/delete")
    public String delete (@PathVariable("id") Integer id) {
         gangService.delete(id); 
        return "redirect:/gangs";        
    }

}
