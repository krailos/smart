package com.krailo.smart.exception;

// помилка оплати, яку треба показати користувачу: сума не більше нуля, повторне сторно тощо
public class PaymentException extends BusinessException {
    public PaymentException(String message) {
        super(message);
    }
}
