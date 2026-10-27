package com.krailo.smart.exception;

// Запис не знайдено в базі (неіснуючий id в адресі або у формі) — GlobalExceptionHandler показує сторінку 404.
// Власний клас замість jakarta.persistence.EntityNotFoundException: той кидає й сам Hibernate,
// а цей — лише наш код, тож у лозі одразу видно, що саме і з яким id шукали.
public class NotFoundException extends RuntimeException {

    // new NotFoundException("Учень", 5) → "Учень з id=5 не знайдено"
    public NotFoundException(String what, Object id) {
        super(what + " з id=" + id + " не знайдено");
    }

    public NotFoundException(String message) {
        super(message);
    }
}
