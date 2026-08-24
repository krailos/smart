package com.krailo.smart.controller;

import com.krailo.smart.service.BalanceStudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
@AllArgsConstructor
public class BalanceStudentController {

    BalanceStudentService balanceStudentService;

    @GetMapping("/recordsStudents")
    public String findAll(Model model) {
        model.addAttribute("balances", balanceStudentService.findAll());
        return "recordsStudents";
    }
}
