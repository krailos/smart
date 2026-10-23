package com.krailo.smart.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Обробка "не знайдено": запис у БД (EntityNotFoundException — більшість сервісів,
    // ResourceNotFoundException — StudentService), неіснуюча адреса (NoResourceFoundException)
    // або id в адресі не число, напр. /teachers/abc (MethodArgumentTypeMismatchException).
    // Параметр — спільний батько Exception, бо NoResourceFoundException не є RuntimeException.
    @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class, NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound (Exception ex, Model model){

        log.error("Ось помилка : {}", ex.getMessage(), ex);

        // технічний ex.getMessage() — тільки в лог (рядок вище), користувачеві — зрозумілий текст
        model.addAttribute("message", "Можливо, адреса містить помилку або запис було видалено. Спробуйте повернутися на головну.");
        model.addAttribute("error", "Ресурс відсутній");
        model.addAttribute("status", 404);
        return "error/404";
    }

    // Обробка інших помилок. stackTrace користувачеві не показуємо — він є в smart.log (log.error нижче)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, Model model) {

        log.error("Ось помилка : {}", ex.getMessage(), ex);

        model.addAttribute("status", 500);
        model.addAttribute("error", "Внутрішня помилка сервера");
        model.addAttribute("message", "На сервері сталася непередбачувана помилка. Ми вже працюємо над її вирішенням.");
        return "error/error";
    }

    }
