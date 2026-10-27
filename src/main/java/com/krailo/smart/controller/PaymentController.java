package com.krailo.smart.controller;

import com.krailo.smart.dto.PaymentDto;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.PaymentReceivingService;
import com.krailo.smart.service.PaymentService;
import com.krailo.smart.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payments")
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final StudentService studentService;
    private PaymentReceivingService paymentReceivingService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("payments", paymentService.findAll());      
        return "payments";
    }
    
    
    
    // перегляд оплати (тільки читання)
    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
          model.addAttribute("payment", paymentService.findById(id));
        return "payment";
    }
    
    
    @GetMapping("/new")
    public String createForm (Model model) {
    model.addAttribute("students", studentService.findAll());    
    return "paymentNew";
    }
    
    
    @PostMapping("/create")
   // @ResponseStatus(HttpStatus.CREATED)
    public String create ( @ModelAttribute PaymentDto paymentDto, Model model) {
        try {
            // оплата + журнал транзакцій + баланс — одна транзакція (див. PaymentReceivingService)
            paymentReceivingService.receivePayment(paymentDto);
        } catch (BusinessException e) {
            model.addAttribute("students", studentService.findAll());
            model.addAttribute("errorMessage", e.getMessage());
            return "paymentNew";
        }
        return "redirect:/payments";
    }

    // Оплати не редагуються і не видаляються — помилкову оплату сторнують
    // (нова оплата з мінусовою сумою; оригінал лишається в історії)
    @PostMapping("/{id}/storno")
    public String storno (@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            paymentReceivingService.stornoPayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Оплату сторновано");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/payments";
    }

}
