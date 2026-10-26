package com.krailo.smart.service;

import com.krailo.smart.entity.User;
import com.krailo.smart.enumeration.Role;
import com.krailo.smart.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Вхід у застосунок: DaoAuthenticationProvider — те саме, що робить Spring при натисканні «Увійти»:
// знаходить користувача за логіном (UserService) і перевіряє пароль через PasswordEncoder
@SpringBootTest
@ActiveProfiles("test")
class PasswordLoginTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldStoreOnlyHash_AndLoginWithRightPasswordOnly() {
        // Given: у базі лише хеш пароля
        String hash = passwordEncoder.encode("secret123");
        userRepository.save(User.builder().name("anna").password(hash).role(Role.TEACHER).build());

        // хеш не містить пароля і починається з префікса алгоритму
        assertThat(hash).startsWith("{bcrypt}$2a$").doesNotContain("secret123");

        // When & Then: правильний пароль — вхід з роллю, неправильний — відмова
        Authentication ok = login("anna", "secret123");
        assertThat(ok.isAuthenticated()).isTrue();
        // (Spring Security 7 додає ще мітку FACTOR_PASSWORD — "вхід підтверджено паролем")
        assertThat(ok.getAuthorities()).extracting("authority").contains("TEACHER");

        assertThatThrownBy(() -> login("anna", "secret124")).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldGiveDifferentHashes_ForSamePassword() {
        // сіль: той самий пароль — різні хеші, але обидва перевіряються
        String first = passwordEncoder.encode("123");
        String second = passwordEncoder.encode("123");

        assertThat(first).isNotEqualTo(second);
        assertThat(passwordEncoder.matches("123", first)).isTrue();
        assertThat(passwordEncoder.matches("123", second)).isTrue();
    }

    @Test
    void shouldStillAcceptOldNoopPassword() {
        // старий формат {noop} теж розпізнається — перехід на BCrypt нікого не блокує
        userRepository.save(User.builder().name("old").password("{noop}123").role(Role.ADMIN).build());

        assertThat(login("old", "123").isAuthenticated()).isTrue();
    }

    private Authentication login(String name, String password) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider.authenticate(new UsernamePasswordAuthenticationToken(name, password));
    }
}
