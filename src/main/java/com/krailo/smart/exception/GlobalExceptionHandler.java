package com.krailo.smart.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Обробка "не знайдено": запис у БД (EntityNotFoundException — більшість сервісів,
    // ResourceNotFoundException — StudentService) або неіснуюча адреса (NoResourceFoundException).
    // Параметр — спільний батько Exception, бо NoResourceFoundException не є RuntimeException.
    @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound (Exception ex, Model model){

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
