package com.krailo.smart.controller;

import com.krailo.smart.service.TransactionStudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class TransactionsStudentsController {

    private TransactionStudentService transactionStudentService;

    @GetMapping("/student-transactions")
    String findAll(Model model) {
        model.addAttribute("transactionsStudents", transactionStudentService.findAll());
        return "transactionsStudents";
    }
}
