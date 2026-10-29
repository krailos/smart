package com.krailo.smart.service;

import com.krailo.smart.dto.OpeningBalanceDto;
import com.krailo.smart.dto.PaymentDto;
import com.krailo.smart.entity.Student;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.exception.PaymentException;
import com.krailo.smart.repository.PaymentRepository;
import com.krailo.smart.repository.StudentRepository;
import com.krailo.smart.repository.TransactionStudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

// Інтеграційний тест: справжні сервіси + база H2, перевіряємо транзакцію прийому оплати.
// Клас навмисно БЕЗ @Transactional — щоб бачити, що реально збереглось у базі.
@SpringBootTest
@ActiveProfiles("test")
class PaymentReceivingServiceTest {

    @Autowired private PaymentReceivingService paymentReceivingService;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private TransactionStudentRepository transactionStudentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    // "шпигун": працює як справжній сервіс, але в окремому тесті можемо змусити його впасти
    @MockitoSpyBean private TransactionStudentService transactionStudentService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = studentRepository.save(Student.builder()
                .firstName("Оля").lastName("Петренко")
                .contactName("мама Оля").phone("0670000000")
                .gender(Gender.ДІВЧИНА).studentStatus(StudentStatus.УЧЕНЬ)
                .balance(BigDecimal.ZERO)
                .build());
    }

    @AfterEach
    void cleanUp() {
        // спершу сторно (воно посилається на оригінальну оплату), потім решта
        jdbcTemplate.update("DELETE FROM transaction_student");
        jdbcTemplate.update("DELETE FROM payment WHERE storno_of_id IS NOT NULL");
        for (String table : List.of("payment", "student")) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }

    @Test
    void shouldSavePaymentAndIncreaseBalance() {
        // When: учень заплатив 500
        paymentReceivingService.receivePayment(paymentDto(500));

        // Then: оплата, запис у журналі та новий баланс — разом
        assertThat(paymentRepository.count()).isEqualTo(1);
        assertThat(transactionStudentRepository.findAll()).singleElement()
                .satisfies(t -> {
                    assertThat(t.getCredit()).isEqualByComparingTo("500");
                    assertThat(t.getBalance()).isEqualByComparingTo("500");
                });
        assertThat(studentRepository.findById(student.getId()).orElseThrow().getBalance()).isEqualByComparingTo("500");
    }

    @Test
    void shouldNotSavePayment_WhenBalanceUpdateFails() {
        // Given: зарахування на баланс падає (імітуємо збій)
        doThrow(new RuntimeException("збій при зарахуванні"))
                .when(transactionStudentService).createByPayment(any());

        // When
        assertThatThrownBy(() -> paymentReceivingService.receivePayment(paymentDto(500)))
                .hasMessageContaining("збій при зарахуванні");

        // Then: оплата теж не збереглась — немає "оплати без балансу"
        assertThat(paymentRepository.count()).isZero();
        assertThat(transactionStudentRepository.count()).isZero();
        assertThat(studentRepository.findById(student.getId()).orElseThrow().getBalance()).isZero();
    }

    @Test
    void shouldNotSavePayment_WhenValueIsNotPositive() {
        // мінусові суми — тільки через сторнування
        assertThatThrownBy(() -> paymentReceivingService.receivePayment(paymentDto(-500)))
                .isInstanceOf(PaymentException.class);
        assertThatThrownBy(() -> paymentReceivingService.receivePayment(paymentDto(0)))
                .isInstanceOf(PaymentException.class);

        assertThat(paymentRepository.count()).isZero();
    }

    @Test
    void shouldNotSavePayment_WhenMoreThanTwoDecimals() {
        // 100.555 база мовчки округлила б до 100.56 — сервіс відмовляє явно
        PaymentDto tooPrecise = new PaymentDto(null, null, student.getId(), new BigDecimal("100.555"),
                LocalDate.of(2026, 10, 24), "тест", null, null, null);
        assertThatThrownBy(() -> paymentReceivingService.receivePayment(tooPrecise))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("двох знаків");

        assertThat(paymentRepository.count()).isZero();
        assertThat(studentRepository.findById(student.getId()).orElseThrow().getBalance()).isZero();
    }

    @Test
    void shouldStornoPayment_AndReturnBalanceBack() {
        // Given: учень заплатив 500 (помилково)
        PaymentDto original = paymentReceivingService.receivePayment(paymentDto(500));

        // When: сторнуємо
        PaymentDto storno = paymentReceivingService.stornoPayment(original.getId());

        // Then: оригінал лишився, з'явилось сторно -500, яке посилається на оригінал; баланс повернувся до 0
        assertThat(paymentRepository.count()).isEqualTo(2);
        assertThat(storno.getValue()).isEqualByComparingTo("-500");
        assertThat(storno.getStornoOfId()).isEqualTo(original.getId());
        assertThat(paymentRepository.existsByStornoOfId(original.getId())).isTrue();
        assertThat(transactionStudentRepository.count()).isEqualTo(2);
        assertThat(studentRepository.findById(student.getId()).orElseThrow().getBalance()).isZero();
    }

    @Test
    void shouldNotStornoTwice() {
        // Given: оплату вже сторновано
        PaymentDto original = paymentReceivingService.receivePayment(paymentDto(500));
        PaymentDto storno = paymentReceivingService.stornoPayment(original.getId());

        // When & Then: повторне сторно і сторно сторна — заборонені
        assertThatThrownBy(() -> paymentReceivingService.stornoPayment(original.getId()))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("уже сторновано");
        assertThatThrownBy(() -> paymentReceivingService.stornoPayment(storno.getId()))
                .isInstanceOf(PaymentException.class);

        // баланс не змінився: 500 - 500 = 0, жодних зайвих записів
        assertThat(paymentRepository.count()).isEqualTo(2);
        assertThat(studentRepository.findById(student.getId()).orElseThrow().getBalance()).isZero();
    }

    // ---------- початковий баланс ----------

    @Test
    void shouldSaveOpeningBalance_PrepaymentOrDebt() {
        // When: учень прийшов з боргом 1200 (облік вівся до програми)
        PaymentDto opening = paymentReceivingService.receiveOpeningBalance(student.getId(), openingDto("-1200", 2026, 9, 1));

        // Then: особлива "оплата" з позначкою, запис у журналі, баланс -1200; примітка за замовчуванням
        assertThat(opening.getOpeningBalance()).isTrue();
        assertThat(opening.getDescription()).isEqualTo("Початковий баланс");
        assertThat(transactionStudentRepository.findAll()).singleElement()
                .satisfies(t -> assertThat(t.getBalance()).isEqualByComparingTo("-1200"));
        assertThat(studentRepository.findById(student.getId()).orElseThrow().getBalance()).isEqualByComparingTo("-1200");
    }

    @Test
    void shouldNotSaveOpeningBalance_WhenZero() {
        assertThatThrownBy(() -> paymentReceivingService.receiveOpeningBalance(student.getId(), openingDto("0", 2026, 9, 1)))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("нульовим");
        assertThat(paymentRepository.count()).isZero();
    }

    @Test
    void shouldAllowOpeningBalanceOnce_AndAgainAfterStorno() {
        // Given: внесли 500, але насправді було 700
        PaymentDto wrong = paymentReceivingService.receiveOpeningBalance(student.getId(), openingDto("500", 2026, 9, 1));

        // When & Then: другий раз — не можна
        assertThatThrownBy(() -> paymentReceivingService.receiveOpeningBalance(student.getId(), openingDto("700", 2026, 9, 1)))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("вже є початковий баланс");

        // When: сторнуємо помилковий — сторно тією самою датою і теж з позначкою
        PaymentDto storno = paymentReceivingService.stornoPayment(wrong.getId());
        assertThat(storno.getDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(storno.getOpeningBalance()).isTrue();

        // Then: тепер можна внести правильний
        paymentReceivingService.receiveOpeningBalance(student.getId(), openingDto("700", 2026, 9, 1));
        assertThat(studentRepository.findById(student.getId()).orElseThrow().getBalance()).isEqualByComparingTo("700");
    }

    @Test
    void shouldNotSaveOpeningBalance_AfterFirstOperation() {
        // Given: у програмі вже є оплата учня від 24.10
        paymentReceivingService.receivePayment(paymentDto(500));

        // When & Then: залишок на 25.10 — пізніше за першу операцію, не можна
        assertThatThrownBy(() -> paymentReceivingService.receiveOpeningBalance(student.getId(), openingDto("300", 2026, 10, 25)))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("24.10.2026");

        // а на 24.10 (той самий день) — можна
        paymentReceivingService.receiveOpeningBalance(student.getId(), openingDto("300", 2026, 10, 24));
        assertThat(studentRepository.findById(student.getId()).orElseThrow().getBalance()).isEqualByComparingTo("800");
    }

    private OpeningBalanceDto openingDto(String value, int year, int month, int day) {
        return new OpeningBalanceDto(new BigDecimal(value), LocalDate.of(year, month, day), null);
    }

    private PaymentDto paymentDto(int value) {
        return new PaymentDto(null, null, student.getId(), BigDecimal.valueOf(value), LocalDate.of(2026, 10, 24), "тест", null, null, null);
    }
}
