package com.krailo.smart.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Обробка кастомної помилки
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleResourceNotFound (ResourceNotFoundException ex, Model model){

        log.error("Ось помилка : {}", ex.getMessage(), ex);

        model.addAttribute("message", ex.getMessage());
        model.addAttribute("error", "Ресурс відсутній");
        model.addAttribute("status", 404);
        model.addAttribute("stackTrace", getStackTraceAsString(ex));
        return "error/404";
    }

    // Обробка інших помилок
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {

        log.error("Ось помилка : {}", ex.getMessage(), ex);

        model.addAttribute("status", 500);
        model.addAttribute("error", "Внутрішня помилка сервера");
        model.addAttribute("message", "На сервері сталася непередбачувана помилка. Ми вже працюємо над її вирішенням.");
        model.addAttribute("stackTrace", getStackTraceAsString(ex));
        return "error/error";
    }

    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    }
