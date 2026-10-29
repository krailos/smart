package com.krailo.smart.controller;

import com.krailo.smart.dto.PaymentDto;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.PaymentReceivingService;
import com.krailo.smart.service.PaymentService;
import com.krailo.smart.service.StudentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

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
        // порожня оплата для th:object форми; дата за замовчуванням — сьогодні
        model.addAttribute("payment", new PaymentDto(null, null, null, null, LocalDate.now(), null, null, null, null));
        model.addAttribute("students", studentService.findAll());
        return "paymentNew";
    }


    @PostMapping("/create")
   // @ResponseStatus(HttpStatus.CREATED)
    // @Valid — перевірити анотації PaymentDto (@NotNull, @DecimalMin…) ще до сервісу.
    // BindingResult (обов'язково одразу після перевіряємого параметра) — сюди Spring складає помилки
    // замість того, щоб кинути виняток; ("payment") — під цим іменем DTO і помилки потраплять у шаблон
    public String create (@Valid @ModelAttribute("payment") PaymentDto paymentDto, BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            // та сама форма: введені значення й текст помилки біля кожного поля (th:errors у шаблоні)
            model.addAttribute("students", studentService.findAll());
            return "paymentNew";
        }
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
