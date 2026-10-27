package com.krailo.smart.exception;

// конфлікт у розкладі (вчитель, аудиторія чи група вже зайняті в цей час) або неправильний час заняття
public class ScheduleConflictException extends BusinessException {
    public ScheduleConflictException(String message) {
        super(message);
    }
}
