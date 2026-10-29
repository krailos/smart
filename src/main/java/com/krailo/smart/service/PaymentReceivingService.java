package com.krailo.smart.service;

import com.krailo.smart.dto.OpeningBalanceDto;
import com.krailo.smart.dto.PaymentDto;
import com.krailo.smart.entity.Payment;
import com.krailo.smart.exception.PaymentException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Прийом оплати: оплата + запис у журналі транзакцій + новий баланс учня.
// Окремий сервіс, бо TransactionStudentService уже залежить від PaymentService
// (якби PaymentService залежав від TransactionStudentService — було б коло залежностей).
@Transactional
@Service
@AllArgsConstructor
public class PaymentReceivingService {

    private PaymentService paymentService;
    private TransactionStudentService transactionStudentService;

    // ВСЕ в одній транзакції: якщо зарахування на баланс впаде,
    // оплата теж не збережеться (не буде оплати, яка "не потрапила" в баланс).
    public PaymentDto receivePayment(PaymentDto paymentDto) {
        // від'ємні суми — тільки через сторнування (щоб було видно, яку оплату виправили)
        // null — поле суми у формі лишили порожнім
        if (paymentDto.getValue() == null || paymentDto.getValue().signum() <= 0) {
            throw new PaymentException("Сума оплати має бути більшою за нуль");
        }
        // більше двох знаків база мовчки округлила б (100.555 -> 100.56); браузер це теж ловить (step), але не завжди
        if (paymentDto.getValue().stripTrailingZeros().scale() > 2) {
            throw new PaymentException("Сума оплати — не більше двох знаків після коми");
        }
        // 1. оплата
        PaymentDto paymentDtoWithId = paymentService.create(paymentDto);
        // 2. запис у журналі транзакцій і новий баланс учня
        transactionStudentService.createByPayment(paymentDtoWithId);
        return paymentDtoWithId;
    }

    // Початковий баланс учня: особлива оплата (перевірки — у PaymentService.createOpeningBalance)
    // + запис у журналі + новий баланс — одна транзакція, як і звичайна оплата
    public PaymentDto receiveOpeningBalance(Integer studentId, OpeningBalanceDto openingBalanceDto) {
        PaymentDto opening = paymentService.createOpeningBalance(studentId, openingBalanceDto);
        transactionStudentService.createByPayment(opening);
        return opening;
    }

    // Сторнування: нова оплата з мінусовою сумою + запис у журналі + зменшення балансу — одна транзакція.
    // Оригінальна оплата не змінюється і лишається в історії.
    public PaymentDto stornoPayment(Integer paymentId) {
        Payment original = paymentService.findByIdEntity(paymentId);
        PaymentDto storno = paymentService.createStorno(original);
        // createByPayment додає суму до балансу — для сторно вона від'ємна, тож баланс зменшиться
        transactionStudentService.createByPayment(storno);
        return storno;
    }
}
