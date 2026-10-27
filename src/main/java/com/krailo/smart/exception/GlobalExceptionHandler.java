package com.krailo.smart.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Обробка "не знайдено": запис у БД (NotFoundException — наші сервіси; EntityNotFoundException — сам Hibernate,
    // якщо пов'язаного запису не стало), неіснуюча адреса (NoResourceFoundException)
    // або id в адресі не число, напр. /teachers/abc (MethodArgumentTypeMismatchException).
    // Параметр — спільний батько Exception, бо NoResourceFoundException не є RuntimeException.
    @ExceptionHandler({NotFoundException.class, EntityNotFoundException.class, NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound (Exception ex, Model model){

        // 404 — звичайна ситуація (описка в адресі, стара закладка), а не збій: один рядок warn без stack trace,
        // щоб справжні помилки (error нижче) не тонули в шумі smart.log
        log.warn("404: {}", ex.getMessage());

        // технічний ex.getMessage() — тільки в лог (рядок вище), користувачеві — зрозумілий текст
        model.addAttribute("message", "Можливо, адреса містить помилку або запис було видалено. Спробуйте повернутися на головну.");
        model.addAttribute("error", "Ресурс відсутній");
        model.addAttribute("status", 404);
        return "error/404";
    }

    // "Заборонено": вчитель відкрив чуже заняття (CurrentUser.requireOwnSchedule).
    // Без цього обробника AccessDeniedException потрапила б у загальний нижче і стала б "500"
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex) {
        log.warn("Доступ заборонено: {}", ex.getMessage());
        return "error/403";
    }

    // Запасний варіант для порушень бізнес-правил (BusinessException і нащадки: Lesson-, Payment-, PriceNotFound-,
    // ScheduleConflictException). Зазвичай контролер сам ловить їх і показує текст над формою;
    // якщо десь try/catch немає — людина все одно бачить зрозумілий текст правила, а не "500".
    // Це очікувана ситуація, а не збій — у лог рівнем info, без stack trace
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessException(BusinessException ex, Model model) {
        log.info("Порушено правило: {}", ex.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("error", "Дію не виконано");
        model.addAttribute("message", ex.getMessage());
        return "error/error";
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
