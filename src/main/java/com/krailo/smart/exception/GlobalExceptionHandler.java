package com.krailo.smart.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // додати логування помилки

    // Обробка кастомної помилки
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound (ResourceNotFoundException ex, Model model){
        model.addAttribute("errorMesssage", ex.getMessage());
        model.addAttribute("error", "Ресурс відсутній");
        model.addAttribute("status", 404);
        return "error/404";
    }

    // Обробка інших помилок
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {

        // додати логування помилки

        model.addAttribute("status", 500);
        model.addAttribute("error", "Внутрішня помилка сервера");
        model.addAttribute("message", "На сервері сталася непередбачувана помилка. Ми вже працюємо над її вирішенням.");

        return "error/error";
    }

    }
