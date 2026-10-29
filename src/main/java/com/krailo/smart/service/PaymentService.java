package com.krailo.smart.service;

import com.krailo.smart.dto.OpeningBalanceDto;
import com.krailo.smart.dto.PaymentDto;
import com.krailo.smart.entity.Payment;
import com.krailo.smart.entity.Student;
import com.krailo.smart.exception.PaymentException;
import com.krailo.smart.mapper.PaymentMapper;
import com.krailo.smart.repository.PaymentRepository;
import com.krailo.smart.repository.TransactionStudentRepository;
import com.krailo.smart.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Transactional
@Service
@AllArgsConstructor
public class PaymentService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private PaymentRepository paymentRepository;
    private PaymentMapper paymentMapper;
    private StudentService studentService;
    private TransactionStudentRepository transactionStudentRepository;

    public List<PaymentDto> findAll() {
        Set<Integer> stornedIds = paymentRepository.findStornedIds();
        return paymentRepository.findAll().stream()
                .map(payment -> paymentMapper.mapEntityToDto(payment, stornedIds.contains(payment.getId())))
                .toList();
    }

    public Payment findByIdEntity(Integer id) {
        return paymentRepository.findById(id).orElseThrow(() -> new NotFoundException("Оплата", id));
    }

    public PaymentDto findById(Integer id) {
        return paymentRepository.findById(id).map(paymentMapper::mapEntityToDto)
                .orElseThrow(() -> new NotFoundException("Оплата", id));
    }
    
    public List<Payment> findAllByStudentEntity(Student student) {
        return paymentRepository.findAllByStudent(student);
    }


    public PaymentDto create(PaymentDto paymentDto) {
        Payment payment = paymentRepository.save(paymentMapper.mapDtoToEntityForCreate(paymentDto));
        return paymentMapper.mapEntityToDto(payment);
    }

    // Початковий баланс учня (облік вівся й до програми): особлива оплата з позначкою openingBalance.
    // + передоплата, - борг. Один діючий на учня; дата — не пізніше першої операції учня в програмі,
    // інакше у виписці залишок "з'явився" б посеред історії
    public PaymentDto createOpeningBalance(Integer studentId, OpeningBalanceDto dto) {
        Student student = studentService.findByIdEntity(studentId);
        if (dto.getValue() == null || dto.getValue().signum() == 0) {
            throw new PaymentException("Початковий баланс не може бути нульовим: для нуля нічого вносити не треба");
        }
        if (dto.getValue().stripTrailingZeros().scale() > 2) {
            throw new PaymentException("Сума — не більше двох знаків після коми");
        }
        if (paymentRepository.existsActiveOpeningBalance(studentId)) {
            throw new PaymentException("У учня вже є початковий баланс. Щоб змінити суму, сторнуйте його у списку платежів"
                    + " і внесіть заново");
        }
        transactionStudentRepository.findFirstOperationDate(studentId)
                .filter(first -> dto.getDate().isAfter(first))
                .ifPresent(first -> {
                    throw new PaymentException(String.format("Перший урок або оплата учня — %s; вкажіть дату не пізніше",
                            first.format(DATE_FORMAT)));
                });
        Payment opening = Payment.builder()
                .student(student)
                .value(dto.getValue())
                .date(dto.getDate())
                .description(dto.getDescription() == null || dto.getDescription().isBlank()
                        ? "Початковий баланс" : dto.getDescription().trim())
                .openingBalance(true)
                .build();
        return paymentMapper.mapEntityToDto(paymentRepository.saveAndFlush(opening));
    }

    // Оплати не редагуються і не видаляються (як у бухгалтерії) — помилкову оплату сторнують:
    // створюємо нову оплату з протилежною сумою, яка посилається на оригінал.
    public PaymentDto createStorno(Payment original) {
        if (original.getStornoOf() != null) {
            throw new PaymentException("Сторно не можна сторнувати");
        }
        if (paymentRepository.existsByStornoOfId(original.getId())) {
            throw new PaymentException(String.format("Оплату від %s на %s грн уже сторновано",
                    original.getDate(), original.getValue()));
        }
        // сторно початкового балансу — тією самою датою: "залишок на ту дату записали неправильно",
        // тож у виписці за будь-який період помилкового залишку не видно. Звичайна оплата — сьогоднішньою датою
        Payment storno = Payment.builder()
                .student(original.getStudent())
                .value(original.getValue().negate())
                .date(original.isOpeningBalance() ? original.getDate() : LocalDate.now())
                .description(String.format(original.isOpeningBalance() ? "Сторно початкового балансу від %s" : "Сторно оплати від %s",
                        original.getDate()))
                .stornoOf(original)
                .openingBalance(original.isOpeningBalance())
                .build();
        return paymentMapper.mapEntityToDto(paymentRepository.saveAndFlush(storno));
    }

}
