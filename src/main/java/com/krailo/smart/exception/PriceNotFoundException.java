package com.krailo.smart.exception;

// для курсу немає ціни на дату уроку — нарахувати оплату неможливо
public class PriceNotFoundException extends BusinessException {
    public PriceNotFoundException(String message) {
        super(message);
    }
}
