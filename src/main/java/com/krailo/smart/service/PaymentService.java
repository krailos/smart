package com.krailo.smart.service;

import com.krailo.smart.dto.PaymentDto;
import com.krailo.smart.entity.Payment;
import com.krailo.smart.entity.Student;
import com.krailo.smart.exception.PaymentException;
import com.krailo.smart.mapper.PaymentMapper;
import com.krailo.smart.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
public class PaymentService {

    private PaymentRepository paymentRepository;
    private PaymentMapper paymentMapper;

    public List<PaymentDto> findAll() {
        return paymentRepository.findAll().stream().map(paymentMapper::mapEntityToDto).toList();
    }

    public Payment findByIdEntity(Integer id) {
        return paymentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format("Payment whith id= %d not exist", id)));
    }

    public PaymentDto findById(Integer id) {
        return paymentRepository.findById(id).map(paymentMapper::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Payment whith id= %d not exist", id)));
    }
    
    public List<Payment> findAllByStudentEntity(Student student) {
        return paymentRepository.findAllByStudent(student);
    }


    public PaymentDto create(PaymentDto paymentDto) {
        return Optional.of(paymentDto).map(paymentMapper::mapDtoToEntityForCreate).map(paymentRepository::save)
                .map(paymentMapper::mapEntityToDto).orElseThrow();
    }

    // Оплати не редагуються і не видаляються (як у бухгалтерії) — помилкову оплату сторнують:
    // створюємо нову оплату з протилежною сумою, яка посилається на оригінал.
    public PaymentDto createStorno(Payment original) {
        if (original.getStornoOf() != null) {
            throw new PaymentException("Сторно не можна сторнувати");
        }
        if (paymentRepository.existsByStornoOfId(original.getId())) {
            throw new PaymentException(String.format("Оплату від %s на %d грн уже сторновано",
                    original.getDate(), original.getValue()));
        }
        Payment storno = Payment.builder()
                .student(original.getStudent())
                .value(-original.getValue())
                .date(LocalDate.now())
                .description(String.format("Сторно оплати від %s", original.getDate()))
                .stornoOf(original)
                .build();
        return paymentMapper.mapEntityToDto(paymentRepository.saveAndFlush(storno));
    }

}
