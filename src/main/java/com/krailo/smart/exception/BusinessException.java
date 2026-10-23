package com.krailo.smart.exception;

// Порушення бізнес-правила, яке треба показати користувачу зрозумілим текстом:
// "не можна архівувати — вчитель є в розкладі", "ціна на цю дату вже є" тощо
public class BusinessException extends RuntimeException{
    public BusinessException(String message) {
        super(message);
    }
}
