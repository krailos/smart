package com.krailo.smart.controller;

import com.krailo.smart.entity.Student;
import com.krailo.smart.report.Bill;
import com.krailo.smart.report.Report;
import com.krailo.smart.service.LessonService;
import com.krailo.smart.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@AllArgsConstructor
public class ReportController {

    private LessonService lessonService;
    private StudentService studentService;
    private Report report;
 

    
    @GetMapping()
    public String findAll () {

        return "reports";
    }

    @GetMapping("/statement")
    public String showAccount(@RequestParam("dateStart") LocalDate dateStart,
                              @RequestParam("dateEnd") LocalDate dateEnd,  @RequestParam("studentId") String studentId, Model model) {
        Student student = studentService.findByIdEntity(Integer.valueOf(studentId));
        Bill bill = report.createBill(student, dateStart, dateEnd);
        model.addAttribute("bill", bill);
        return "reportAccount";

    }

    @GetMapping("/statement/form")
    public String showAccountForm (Model model) {
        // і активні, і архівні: учень в архіві теж може мати борг
        model.addAttribute("students", studentService.findAllIncludingArchived());
        return "reportAccountForm";
    }

}
