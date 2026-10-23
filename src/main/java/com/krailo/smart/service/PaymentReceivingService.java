package com.krailo.smart.service;

import com.krailo.smart.dto.PaymentDto;
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
        // 1. оплата
        PaymentDto paymentDtoWithId = paymentService.create(paymentDto);
        // 2. запис у журналі транзакцій і новий баланс учня
        transactionStudentService.createByPayment(paymentDtoWithId);
        return paymentDtoWithId;
    }
}
