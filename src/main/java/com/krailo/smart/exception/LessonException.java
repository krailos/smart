package com.krailo.smart.exception;

// помилка уроку, яку треба показати користувачу: урок уже сторновано, сторнований урок не редагується тощо
public class LessonException extends BusinessException {
    public LessonException(String message) {
        super(message);
    }
}
