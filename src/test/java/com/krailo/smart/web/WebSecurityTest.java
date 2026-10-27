package com.krailo.smart.web;

import com.krailo.smart.entity.*;
import com.krailo.smart.enumeration.Role;
import com.krailo.smart.enumeration.WeekDay;
import com.krailo.smart.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Веб-шар: запит іде крізь УВЕСЬ справжній ланцюжок — фільтри Spring Security (доступ за ролями, CSRF, вхід),
// перехоплювач тимчасового пароля, контролер, шаблон Thymeleaf. Сервер не запускається — MockMvc імітує браузер.
// Перевіряємо те, що раніше перевіряли тільки вручну на 8081.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebSecurityTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private AudienceRepository audienceRepository;
    @Autowired private GangRepository gangRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Schedule viraSlot;    // заняття, яке веде вчителька Віра
    private Schedule otherSlot;   // заняття іншого вчителя

    @BeforeEach
    void setUp() {
        Subject subject = subjectRepository.save(Subject.builder().name("Англійська").build());
        Audience audience = audienceRepository.save(Audience.builder().name("101").build());
        Teacher vira = teacherRepository.save(Teacher.builder().firstName("Віра").lastName("Петреній").build());
        Teacher other = teacherRepository.save(Teacher.builder().firstName("Олена").lastName("Шашчак").build());
        Gang viraGang = gangRepository.save(Gang.builder().name("Англ-В").subject(subject).teacher(vira).build());
        Gang otherGang = gangRepository.save(Gang.builder().name("Англ-О").subject(subject).teacher(other).build());
        viraSlot = scheduleRepository.save(slot(viraGang, subject, audience, vira, WeekDay.ПОНЕДІЛОК));
        otherSlot = scheduleRepository.save(slot(otherGang, subject, audience, other, WeekDay.ВІВТОРОК));

        userRepository.save(User.builder().name("boss").password(passwordEncoder.encode("boss-pass-1")).role(Role.ADMIN).build());
        userRepository.save(User.builder().name("vira").password(passwordEncoder.encode("vira-pass-1")).role(Role.TEACHER).teacher(vira).build());
        // новий користувач з тимчасовим паролем (його задав адмін)
        userRepository.save(User.builder().name("newbie").password(passwordEncoder.encode("temp-pass-1")).role(Role.ADMIN)
                .mustChangePassword(true).build());
        // користувач в архіві
        userRepository.save(User.builder().name("gone").password(passwordEncoder.encode("gone-pass-1")).role(Role.ADMIN)
                .archived(true).build());
    }

    @AfterEach
    void cleanUp() {
        for (String table : List.of("users", "schedule", "gang", "teacher", "subject", "audience")) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
    }

    // ---------- без входу ----------

    @Test
    void anonymous_SeesOnlyLoginPageAndStyles() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/css/app.css")).andExpect(status().isOk());   // сторінка входу має оформлення
        // будь-яка інша сторінка — на вхід
        for (String url : List.of("/", "/students", "/schedules", "/users")) {
            mvc.perform(get(url)).andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    // ---------- ролі: адмін ----------

    @Test
    void admin_OpensAdminPages() throws Exception {
        for (String url : List.of("/students", "/payments", "/users", "/schedules", "/schedules/week",
                "/reports", "/teachers", "/gangs", "/schedules/new", "/student-discounts")) {
            mvc.perform(get(url).with(admin())).andExpect(status().isOk());
        }
        // будь-яке заняття розкладу
        mvc.perform(get("/schedules/{id}/lesson/new", otherSlot.getId()).with(admin())).andExpect(status().isOk());
    }

    // ---------- ролі: вчитель ----------

    @Test
    void teacher_OpensOnlyOwnPages() throws Exception {
        for (String url : List.of("/", "/schedules", "/lessons", "/my/students", "/account/password")) {
            mvc.perform(get(url).with(teacher())).andExpect(status().isOk());
        }
    }

    @Test
    void teacher_GetsForbidden_OnAdminPagesAndActions() throws Exception {
        for (String url : List.of("/students", "/payments", "/payments/new", "/users", "/schedules/week",
                "/schedules/new", "/reports", "/teachers", "/gangs", "/student-discounts",
                "/teachers/1/gangs", "/lessons/1")) {
            // (сторінку «Дію не виконано» для цієї 403 показує сам Tomcat через /error — MockMvc цього кроку
            //  не імітує, тож тут перевіряємо код; сторінку перевіряє тест чужого заняття нижче)
            mvc.perform(get(url).with(teacher()))
                    .andExpect(status().isForbidden());
        }
        // дії адміна — теж ні (навіть з правильним CSRF-токеном)
        for (String url : List.of("/payments/1/storno", "/lessons/1/storno", "/users/create", "/schedules/create")) {
            mvc.perform(post(url).with(teacher()).with(csrf())).andExpect(status().isForbidden());
        }
    }

    @Test
    void teacher_SeesOnlyOwnSchedule_AndOpensLessonOnlyFromOwnSlot() throws Exception {
        // у списку розкладу — тільки своє заняття (кнопка «Провести урок» лише на нього)
        mvc.perform(get("/schedules").with(teacher()))
                .andExpect(content().string(containsString("/schedules/" + viraSlot.getId() + "/lesson/new")))
                .andExpect(content().string(not(containsString("/schedules/" + otherSlot.getId() + "/lesson/new"))));

        mvc.perform(get("/schedules/{id}/lesson/new", viraSlot.getId()).with(teacher()))
                .andExpect(status().isOk());
        // чуже заняття, навіть якщо вписати адресу вручну
        mvc.perform(get("/schedules/{id}/lesson/new", otherSlot.getId()).with(teacher()).accept(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Дію не виконано")));
        // і підроблена форма збереження уроку з чужим заняттям
        mvc.perform(post("/lessons/create").with(teacher()).with(csrf())
                        .param("scheduleId", String.valueOf(otherSlot.getId())))
                .andExpect(status().isForbidden());
    }

    // ---------- CSRF ----------

    @Test
    void postWithoutCsrfToken_IsRejected() throws Exception {
        // запит без токена (як з чужого сайту) — 403, нічого не створено
        mvc.perform(post("/users/create").with(admin())
                        .param("name", "intruder").param("password", "Hacked2026x").param("role", "ADMIN"))
                .andExpect(status().isForbidden());
        assertThat(userRepository.findUserByNameIgnoreCaseAndArchivedFalse("intruder")).isEmpty();

        // той самий запит з токеном (як зі справжньої форми) — проходить
        mvc.perform(post("/users/create").with(admin()).with(csrf())
                        .param("name", "helper").param("password", "Helper2026x").param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        assertThat(userRepository.findUserByNameIgnoreCaseAndArchivedFalse("helper")).isPresent();
    }

    // ---------- вхід ----------

    @Test
    void login_RightPassword_GoesHome_WrongOrArchived_BackToLogin() throws Exception {
        mvc.perform(formLogin("/login").user("boss").password("boss-pass-1"))
                .andExpect(redirectedUrl("/"));
        mvc.perform(formLogin("/login").user("BOSS").password("boss-pass-1"))   // регістр логіна не важливий
                .andExpect(redirectedUrl("/"));
        mvc.perform(formLogin("/login").user("boss").password("wrong-pass"))
                .andExpect(redirectedUrl("/login?error"));
        mvc.perform(formLogin("/login").user("gone").password("gone-pass-1"))    // архівний
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void login_WithTemporaryPassword_ForcesPasswordChange() throws Exception {
        // вхід з тимчасовим паролем — одразу на зміну пароля
        MockHttpSession session = (MockHttpSession) mvc.perform(formLogin("/login").user("newbie").password("temp-pass-1"))
                .andExpect(redirectedUrl("/account/password"))
                .andReturn().getRequest().getSession();

        // поки не змінив — будь-яка сторінка повертає туди ж
        for (String url : List.of("/", "/students", "/users")) {
            mvc.perform(get(url).session(session)).andExpect(redirectedUrl("/account/password"));
        }
        mvc.perform(get("/account/password").session(session)).andExpect(status().isOk());

        // змінив пароль — замок знято
        mvc.perform(post("/account/password").session(session).with(csrf())
                        .param("currentPassword", "temp-pass-1")
                        .param("newPassword", "own-pass-22").param("repeatPassword", "own-pass-22"))
                .andExpect(redirectedUrl("/"));
        mvc.perform(get("/students").session(session)).andExpect(status().isOk());
    }

    // ---------- сторінки помилок і параметри адреси ----------

    @Test
    void errorPages_ShowFriendlyNotFound() throws Exception {
        // немає такого запису / id не число / немає такої адреси — 404 зі зрозумілим текстом, без stackTrace
        // (/lessons/999999 раніше давав 500 — LessonService.findById брав .get() без перевірки)
        for (String url : List.of("/students/999999", "/lessons/999999", "/students/abc", "/no-such-page")) {
            mvc.perform(get(url).with(admin()).accept(MediaType.TEXT_HTML))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Ресурс відсутній")))
                    // технічних подробиць (stack trace) користувачу не показуємо — вони тільки в лозі
                    .andExpect(content().string(not(containsString("at com.krailo"))))
                    .andExpect(content().string(not(containsString("Caused by"))));
        }
    }

    @Test
    void studentsList_ToleratesBadPagingParams() throws Exception {
        // кнопка «‹» на першій сторінці дає page=-1; size/sortBy можна підробити в адресі — не 500
        for (String query : List.of("page=-1", "size=0", "size=-5", "size=1000000", "sortBy=password", "sortBy=abc")) {
            mvc.perform(get("/students?" + query).with(admin())).andExpect(status().isOk());
        }
    }

    // увійшов адмін / вчителька Віра (логін є в базі — CurrentUser шукає за ним картку вчителя)
    private RequestPostProcessor admin() {
        return user("boss").authorities(Role.ADMIN);
    }

    private RequestPostProcessor teacher() {
        return user("vira").authorities(Role.TEACHER);
    }

    private Schedule slot(Gang gang, Subject subject, Audience audience, Teacher teacher, WeekDay day) {
        return Schedule.builder().gang(gang).subject(subject).audience(audience).teacher(teacher)
                .weekDay(day).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0)).build();
    }
}
