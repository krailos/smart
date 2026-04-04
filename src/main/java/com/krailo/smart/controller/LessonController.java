package com.krailo.smart.controller;

import com.krailo.smart.service.LessonService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/lessons")
@AllArgsConstructor
public class LessonController {

    private LessonService lessonService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("lessons", lessonService.findAll());
        return "/lessons";
    }
    
    
//    
//    @GetMapping("/{id}")
//    public String findById (@PathVariable("id") Integer id, Model model) {
//          model.addAttribute("student", lessonService.findById(id));
//        return "/student";        
//    }
//    
//    
//    
//    @GetMapping("/student/new")
//    public String createForm (Model model) {
//    model.addAttribute("gang", new Student());
//    return "/studentNew";
//    }
//    
//    
//    @PostMapping("/create")
//   // @ResponseStatus(HttpStatus.CREATED)
//    public String create ( @ModelAttribute StudentDto student) {
//        lessonService.create(student);  
//        return "redirect:/students";     
//    }
//    
//    @PostMapping("/{id}/update")
//    public String update (@PathVariable("id") Integer id, @ModelAttribute StudentDto studentDto) {
//           lessonService.update(id, studentDto); 
//        return "redirect:/students";        
//    }
//    
//    @PostMapping("/{id}/delete")
//    public String delete (@PathVariable("id") Integer id) {
//         lessonService.delete(id); 
//        return "redirect:/students";        
//    }

}
