package com.krailo.smart.exception;

// Порушення бізнес-правила, яке треба показати користувачу зрозумілим текстом:
// "не можна архівувати — вчитель є в розкладі", "ціна на цю дату вже є" тощо.
// Батько всіх таких винятків (Lesson-, Payment-, PriceNotFound-, ScheduleConflictException):
// контролеру досить одного catch (BusinessException e), а якщо його немає — GlobalExceptionHandler
// покаже текст правила замість "500"
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
