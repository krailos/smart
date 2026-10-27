package com.krailo.smart.web;

import com.krailo.smart.entity.*;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.Role;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.repository.*;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Форми довідників і карток (@Valid у контролерах + анотації DTO): неправильні дані, надіслані в обхід браузера, —
// та сама форма з текстом біля поля (не 500, не редирект) і в базі нічого не змінилось.
// Форма оплати — окремо, у PaymentFormTest
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FormValidationTest {

    private static final String LONG_51 = "x".repeat(51);
    private static final String LONG_151 = "x".repeat(151);

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AudienceRepository audienceRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private DiscountRepository discountRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private GangRepository gangRepository;
    @Autowired private PriceRepository priceRepository;
    @Autowired private StudentsDiscountsRepository studentsDiscountsRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder().name("boss").password(passwordEncoder.encode("boss-pass-1")).role(Role.ADMIN).build());
    }

    @AfterEach
    void cleanUp() {
        for (String table : List.of("students_discounts", "price", "gang", "student", "teacher", "discount", "subject",
                "audience", "users")) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }

    // ---------- аудиторія ----------

    @Test
    void audience_Create() throws Exception {
        expectFieldError(form("/audiences/create", "name", "", "description", ""), "Вкажіть назву аудиторії");
        expectFieldError(form("/audiences/create", "name", "   ", "description", ""), "Вкажіть назву аудиторії");
        expectFieldError(form("/audiences/create", "name", LONG_51, "description", ""), "не довше 50 символів");
        expectFieldError(form("/audiences/create", "name", "101", "description", LONG_151), "не довше 150 символів");
        assertThat(audienceRepository.count()).isZero();

        mvc.perform(form("/audiences/create", "name", "101", "description", ""))
                .andExpect(redirectedUrl("/audiences"));
        assertThat(audienceRepository.count()).isEqualTo(1);
    }

    @Test
    void audience_Update() throws Exception {
        Audience audience = audienceRepository.save(Audience.builder().name("101").build());
        String url = "/audiences/" + audience.getId() + "/update";
        // після помилки форма знову відправляється на адресу цієї аудиторії (id з адреси потрапив у DTO)
        expectFieldError(form(url, "name", "", "description", ""), "Вкажіть назву аудиторії")
                .andExpect(content().string(containsString("action=\"" + url + "\"")));
        assertThat(audienceRepository.findById(audience.getId()).orElseThrow().getName()).isEqualTo("101");
    }

    // ---------- курс ----------

    @Test
    void subject_CreateAndUpdate() throws Exception {
        expectFieldError(form("/subjects/create", "name", ""), "Вкажіть назву курсу");
        expectFieldError(form("/subjects/create", "name", LONG_51), "не довше 50 символів");
        assertThat(subjectRepository.count()).isZero();

        Subject subject = subjectRepository.save(Subject.builder().name("Англійська").build());
        String url = "/subjects/" + subject.getId() + "/update";
        expectFieldError(form(url, "name", "", "description", LONG_151), "Вкажіть назву курсу")
                .andExpect(content().string(containsString("не довше 150 символів")))
                .andExpect(content().string(containsString("action=\"" + url + "\"")));
        assertThat(subjectRepository.findById(subject.getId()).orElseThrow().getName()).isEqualTo("Англійська");
    }

    // ---------- знижка ----------

    @Test
    void discount_Create() throws Exception {
        expectFieldError(form("/discounts/create", "name", "", "value", "10", "date", "2026-09-01"), "Вкажіть вид знижки");
        expectFieldError(form("/discounts/create", "name", "УБД", "value", "", "date", "2026-09-01"), "Вкажіть розмір знижки");
        expectFieldError(form("/discounts/create", "name", "УБД", "value", "abc", "date", "2026-09-01"), "Має бути ціле число");
        expectFieldError(form("/discounts/create", "name", "УБД", "value", "0", "date", "2026-09-01"), "від 1 до 100%");
        expectFieldError(form("/discounts/create", "name", "УБД", "value", "101", "date", "2026-09-01"), "від 1 до 100%");
        expectFieldError(form("/discounts/create", "name", "УБД", "value", "10", "date", ""), "Вкажіть дату");
        assertThat(discountRepository.count()).isZero();

        mvc.perform(form("/discounts/create", "name", "УБД", "value", "50", "date", "2026-09-01"))
                .andExpect(redirectedUrl("/discounts"));
        // та сама назва з тією ж датою (у базі UNIQUE) — раніше 500, тепер текст біля поля
        expectFieldError(form("/discounts/create", "name", "УБД", "value", "30", "date", "2026-09-01"),
                "Знижка «УБД» з датою 01.09.2026 уже є");
        assertThat(discountRepository.count()).isEqualTo(1);
    }

    @Test
    void discount_Update_SameNameAndDate_IsNotDuplicateOfItself() throws Exception {
        Discount discount = discountRepository.save(Discount.builder().name("УБД").value(50).date(LocalDate.of(2026, 9, 1)).build());
        mvc.perform(form("/discounts/" + discount.getId() + "/update", "name", "УБД", "value", "40", "date", "2026-09-01"))
                .andExpect(redirectedUrl("/discounts"));
        assertThat(discountRepository.findById(discount.getId()).orElseThrow().getValue()).isEqualTo(40);
    }

    // ---------- вчитель ----------

    @Test
    void teacher_CreateAndUpdate() throws Exception {
        expectFieldError(form("/teachers/create", "lastName", "", "firstName", ""), "Вкажіть прізвище")
                .andExpect(content().string(containsString("Вкажіть ім&#39;я")));
        expectFieldError(form("/teachers/create", "lastName", "Петренко", "firstName", "Віра", "email", "not-an-email"),
                "Неправильний email");
        expectFieldError(form("/teachers/create", "lastName", "Петренко", "firstName", "Віра", "birthDate", "abc"),
                "Має бути дата");
        assertThat(teacherRepository.count()).isZero();

        mvc.perform(form("/teachers/create", "lastName", "Петренко", "firstName", "Віра", "birthDate", "1990-05-01"))
                .andExpect(redirectedUrl("/teachers"));
        Teacher teacher = teacherRepository.findAll().get(0);
        String url = "/teachers/" + teacher.getId() + "/update";
        expectFieldError(form(url, "lastName", "", "firstName", "Віра"), "Вкажіть прізвище")
                .andExpect(content().string(containsString("action=\"" + url + "\"")));
        assertThat(teacherRepository.findById(teacher.getId()).orElseThrow().getLastName()).isEqualTo("Петренко");
    }

    // ---------- учень ----------

    @Test
    void student_CreateAndUpdate() throws Exception {
        expectFieldError(form("/students/create", studentFields("gender", "")), "Оберіть стать")
                // після помилки меню «Стать» і «Статус» знову заповнені (контролер поклав варіанти в модель)
                .andExpect(content().string(containsString(">ДІВЧИНА<")))
                .andExpect(content().string(containsString(">УЧЕНЬ<")));
        expectFieldError(form("/students/create", studentFields("phone", "")), "Вкажіть телефон");
        expectFieldError(form("/students/create", studentFields("contactName", "")), "Вкажіть контактну особу");
        expectFieldError(form("/students/create", studentFields("address", LONG_151)), "Не довше 150 символів");
        assertThat(studentRepository.count()).isZero();

        mvc.perform(form("/students/create", studentFields("email", "olia@example.com")))
                .andExpect(redirectedUrl("/students"));
        Student student = studentRepository.findAll().get(0);
        String url = "/students/" + student.getId() + "/update";
        expectFieldError(form(url, studentFields("lastName", "")), "Вкажіть прізвище")
                .andExpect(content().string(containsString("action=\"" + url + "\"")));
        assertThat(studentRepository.findById(student.getId()).orElseThrow().getLastName()).isEqualTo("Петренко");
    }

    // ---------- група ----------

    @Test
    void gang_Create() throws Exception {
        Subject subject = subjectRepository.save(Subject.builder().name("Англійська").build());
        Teacher teacher = teacherRepository.save(Teacher.builder().firstName("Віра").lastName("Петреній").build());
        String subjectId = String.valueOf(subject.getId());
        String teacherId = String.valueOf(teacher.getId());

        // без курсу GangMapper створював курс без id — раніше 500
        expectFieldError(form("/gangs/create", "name", "Англ-1", "subjectId", "", "teacherId", teacherId), "Оберіть курс")
                .andExpect(content().string(containsString(">Англійська<")));   // меню курсів знову заповнене
        expectFieldError(form("/gangs/create", "name", "Англ-1", "subjectId", subjectId, "teacherId", ""), "Оберіть вчителя");
        expectFieldError(form("/gangs/create", "name", "", "subjectId", subjectId, "teacherId", teacherId), "Вкажіть назву групи");
        assertThat(gangRepository.count()).isZero();

        mvc.perform(form("/gangs/create", "name", "Англ-1", "subjectId", subjectId, "teacherId", teacherId))
                .andExpect(redirectedUrl("/gangs"));
        assertThat(gangRepository.count()).isEqualTo(1);
    }

    // ---------- ціна курсу ----------

    @Test
    void price_Create() throws Exception {
        Subject subject = subjectRepository.save(Subject.builder().name("Англійська").build());
        String subjectId = String.valueOf(subject.getId());

        expectFieldError(form("/prices/create", "subjectId", subjectId, "subjectName", "Англійська", "value", "", "date", "2026-09-01"),
                "Вкажіть вартість уроку")
                .andExpect(content().string(containsString("value=\"Англійська\"")));   // назва курсу не зникла
        expectFieldError(form("/prices/create", "subjectId", subjectId, "value", "-1", "date", "2026-09-01"), "не може бути від");
        expectFieldError(form("/prices/create", "subjectId", subjectId, "value", "100.555", "date", "2026-09-01"), "не більше 2 після");
        // без дати раніше 500 (у базі price_date NOT NULL)
        expectFieldError(form("/prices/create", "subjectId", subjectId, "value", "300", "date", ""), "Вкажіть дату");
        assertThat(priceRepository.count()).isZero();

        mvc.perform(form("/prices/create", "subjectId", subjectId, "value", "300", "date", "2026-09-01"))
                .andExpect(redirectedUrl("/subjects"));
        // ціна на ту саму дату — текст біля поля «Діє з», а не повідомлення на іншій сторінці
        expectFieldError(form("/prices/create", "subjectId", subjectId, "value", "350", "date", "2026-09-01"), "уже є");
        assertThat(priceRepository.count()).isEqualTo(1);
    }

    @Test
    void price_Update() throws Exception {
        Subject subject = subjectRepository.save(Subject.builder().name("Англійська").build());
        Price price = priceRepository.save(Price.builder().subject(subject).value(new BigDecimal("300"))
                .date(LocalDate.of(2026, 9, 1)).build());
        String url = "/prices/" + price.getId() + "/update";
        expectFieldError(form(url, "subjectId", String.valueOf(subject.getId()), "subjectName", "Англійська",
                        "value", "-5", "date", "2026-09-01"), "не може бути від")
                .andExpect(content().string(containsString("action=\"" + url + "\"")));
        assertThat(priceRepository.findById(price.getId()).orElseThrow().getValue()).isEqualByComparingTo("300");
    }

    // ---------- знижка учня ----------

    @Test
    void studentDiscount_Create() throws Exception {
        Student student = studentRepository.save(student());
        Subject subject = subjectRepository.save(Subject.builder().name("Англійська").build());
        Discount discount = discountRepository.save(Discount.builder().name("УБД").value(50).date(LocalDate.of(2026, 9, 1)).build());
        String s = String.valueOf(student.getId()), sub = String.valueOf(subject.getId()), d = String.valueOf(discount.getId());

        // порожні id раніше давали 500 (findById(null) у StudentsDiscountsMapper)
        expectFieldError(form("/student-discounts/create", "studentId", "", "subjectId", sub, "discountId", d, "date", "2026-09-01"),
                "Оберіть учня");
        expectFieldError(form("/student-discounts/create", "studentId", s, "subjectId", "", "discountId", d, "date", "2026-09-01"),
                "Оберіть курс");
        expectFieldError(form("/student-discounts/create", "studentId", s, "subjectId", sub, "discountId", "", "date", "2026-09-01"),
                "Оберіть знижку");
        expectFieldError(form("/student-discounts/create", "studentId", s, "subjectId", sub, "discountId", d, "date", ""),
                "Вкажіть дату");
        assertThat(studentsDiscountsRepository.count()).isZero();

        mvc.perform(form("/student-discounts/create", "studentId", s, "subjectId", sub, "discountId", d, "date", "2026-09-01"))
                .andExpect(redirectedUrl("/student-discounts"));
        expectFieldError(form("/student-discounts/create", "studentId", s, "subjectId", sub, "discountId", d, "date", "2026-09-02"),
                "Учень уже має цю знижку на цей курс");
        assertThat(studentsDiscountsRepository.count()).isEqualTo(1);
    }

    // правильна форма учня; changes — поля, які замінити ("поле", "значення")
    private String[] studentFields(String... changes) {
        java.util.Map<String, String> fields = new java.util.LinkedHashMap<>();
        fields.put("lastName", "Петренко");
        fields.put("firstName", "Оля");
        fields.put("contactName", "мама Оля");
        fields.put("phone", "0670000000");
        fields.put("gender", "ДІВЧИНА");
        fields.put("studentStatus", "УЧЕНЬ");
        for (int i = 0; i < changes.length; i += 2) {
            fields.put(changes[i], changes[i + 1]);
        }
        return fields.entrySet().stream().flatMap(e -> java.util.stream.Stream.of(e.getKey(), e.getValue()))
                .toArray(String[]::new);
    }

    private Student student() {
        return Student.builder().firstName("Оля").lastName("Петренко").contactName("мама Оля").phone("0670000000")
                .gender(Gender.ДІВЧИНА).studentStatus(StudentStatus.УЧЕНЬ).balance(BigDecimal.ZERO).build();
    }

    // POST форми від адміна: пари "поле", "значення"
    private MockHttpServletRequestBuilder form(String url, String... fieldValuePairs) {
        MockHttpServletRequestBuilder request = post(url).with(user("boss").authorities(Role.ADMIN)).with(csrf());
        for (int i = 0; i < fieldValuePairs.length; i += 2) {
            request.param(fieldValuePairs[i], fieldValuePairs[i + 1]);
        }
        return request;
    }

    // та сама форма (200, не редирект і не 500) з текстом помилки
    private ResultActions expectFieldError(MockHttpServletRequestBuilder request, String message) throws Exception {
        return mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(message)));
    }
}
