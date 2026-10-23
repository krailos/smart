package com.krailo.smart.exception;

public class ScheduleConflictException extends RuntimeException{
    public ScheduleConflictException(String message) {
        super(message);
    }
}
