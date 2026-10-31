package com.krailo.smart.web;

import com.krailo.smart.entity.User;
import com.krailo.smart.enumeration.Role;
import com.krailo.smart.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Помилка при збереженні розкладу.
// Тиждень: назад на календар, єдиний flash-атрибут — errorMessage (вікно не відкривається, введене не зберігається).
// Список: окрема сторінка форми лишається відкритою з повідомленням і введеними даними.
// Форму без групи сервіс відхиляє ще до звернення до бази, тому тестових даних не треба
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScheduleFormErrorTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder().name("boss").password(passwordEncoder.encode("boss-pass-1")).role(Role.ADMIN).build());
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void weekCreate_ReturnsToWeekWithMessageOnly() throws Exception {
        expectOnlyMessage("/schedules/week/create");
    }

    @Test
    void weekUpdate_ReturnsToWeekWithMessageOnly() throws Exception {
        expectOnlyMessage("/schedules/week/1/update");
    }

    @Test
    void listCreate_KeepsFormWithEnteredData() throws Exception {
        expectFormWithEnteredData("/schedules/create");
    }

    @Test
    void listUpdate_KeepsFormWithEnteredData() throws Exception {
        expectFormWithEnteredData("/schedules/1/update");
    }

    private void expectOnlyMessage(String url) throws Exception {
        mvc.perform(asAdmin(url))
                .andExpect(redirectedUrl("/schedules/week"))
                .andExpect(flash().attribute("errorMessage", "Оберіть групу"))
                .andExpect(flash().attributeCount(1));
    }

    private void expectFormWithEnteredData(String url) throws Exception {
        mvc.perform(asAdmin(url).param("startTime", "10:15"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Оберіть групу")))
                .andExpect(content().string(containsString("value=\"10:15\"")));
    }

    private MockHttpServletRequestBuilder asAdmin(String url) {
        return post(url).with(user("boss").authorities(Role.ADMIN)).with(csrf());
    }
}
