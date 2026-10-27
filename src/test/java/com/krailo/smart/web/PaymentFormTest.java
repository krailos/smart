package com.krailo.smart.web;

import com.krailo.smart.entity.Student;
import com.krailo.smart.entity.User;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.Role;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.repository.PaymentRepository;
import com.krailo.smart.repository.StudentRepository;
import com.krailo.smart.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Форма нової оплати: @Valid у PaymentController + анотації PaymentDto.
// Неправильні дані (форму надіслали в обхід браузера) — та сама форма з текстом біля поля, а не 500, і нічого не збережено
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentFormTest {

    @Autowired private MockMvc mvc;
    @Autowired private StudentRepository studentRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Student student;

    @BeforeEach
    void setUp() {
        student = studentRepository.save(Student.builder()
                .firstName("Оля").lastName("Петренко")
                .contactName("мама Оля").phone("0670000000")
                .gender(Gender.ДІВЧИНА).studentStatus(StudentStatus.УЧЕНЬ)
                .balance(BigDecimal.ZERO)
                .build());
        userRepository.save(User.builder().name("boss").password(passwordEncoder.encode("boss-pass-1")).role(Role.ADMIN).build());
    }

    @AfterEach
    void cleanUp() {
        for (String table : List.of("transaction_student", "payment", "student", "users")) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }

    @Test
    void validPayment_IsSaved() throws Exception {
        mvc.perform(paymentForm(String.valueOf(student.getId()), "500", "2026-10-27", "готівка"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments"));
        assertThat(paymentRepository.count()).isEqualTo(1);
    }

    @Test
    void invalidPayment_ShowsFieldMessage_AndSavesNothing() throws Exception {
        // { учень, сума, дата, примітка, очікуваний текст під полем }
        String id = String.valueOf(student.getId());
        List<String[]> cases = List.of(
                new String[]{"", "500", "2026-10-27", "", "Оберіть учня"},
                new String[]{id, "", "2026-10-27", "", "Вкажіть суму"},
                new String[]{id, "abc", "2026-10-27", "", "Має бути число"},
                new String[]{id, "-5", "2026-10-27", "", "Сума оплати має бути більшою за нуль"},
                new String[]{id, "1000000000", "2026-10-27", "", "до 8 цифр перед комою"},
                new String[]{id, "10.555", "2026-10-27", "", "не більше 2 після"},
                new String[]{id, "500", "", "", "Вкажіть дату"},
                new String[]{id, "500", "2026-10-27", "x".repeat(151), "не довше 150 символів"});
        for (String[] c : cases) {
            mvc.perform(paymentForm(c[0], c[1], c[2], c[3]))
                    .andExpect(status().isOk())                          // та сама форма, не редирект і не 500
                    .andExpect(content().string(containsString(c[4])));
        }
        assertThat(paymentRepository.count()).isZero();
    }

    private MockHttpServletRequestBuilder paymentForm(String studentId, String value, String date, String description) {
        return post("/payments/create").with(user("boss").authorities(Role.ADMIN)).with(csrf())
                .param("studentId", studentId).param("value", value)
                .param("date", date).param("description", description);
    }
}
