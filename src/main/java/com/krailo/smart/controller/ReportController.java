package com.krailo.smart.controller;

import com.krailo.smart.dto.AudienceDto;
import com.krailo.smart.entity.Payment;
import com.krailo.smart.entity.Student;
import com.krailo.smart.report.Bill;
import com.krailo.smart.report.ItemBill;
import com.krailo.smart.report.Report;
import com.krailo.smart.service.LessonService;
import com.krailo.smart.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/account")
    public String showAccount(@RequestParam("dateStart") LocalDate dateStart,
                              @RequestParam("dateEnd") LocalDate dateEnd,  @RequestParam("studentId") String studentId, Model model) {
        Student student = studentService.findByIdEntity(Integer.valueOf(studentId));
        Bill bill = report.createBill(student, dateStart, dateEnd);
        List<ItemBill> itemBills = bill.getItems();
        List<Payment> payments = bill.getPayments();
        model.addAttribute("bill", bill);
        model.addAttribute("items", itemBills);
        model.addAttribute("payments", payments);
        return "reportAccount";

    }

    @GetMapping("/account/form")
    public String showAccountForm (Model model) {
        model.addAttribute("students", studentService.findAll());
        return "reportAccountForm";
    }
    

    
    @PostMapping("/create")
   // @ResponseStatus(HttpStatus.CREATED)
    public String create ( @ModelAttribute AudienceDto audience) {
      
        return "redirect:/audiences";     
    }

}
