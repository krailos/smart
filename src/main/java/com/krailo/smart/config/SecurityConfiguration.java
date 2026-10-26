package com.krailo.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginSuccessHandler loginSuccessHandler) throws Exception {
        // CSRF увімкнено (за замовчуванням): кожна POST-форма несе прихований токен _csrf,
        // Thymeleaf додає його сам у форми з th:action. Запит без токена (напр. з чужого сайту) -> 403
        http.authorizeHttpRequests(auth -> auth
                        // статичні файли (стилі, іконка, логотип) — без логіну, щоб сторінка входу мала оформлення
                        .requestMatchers("/css/**", "/favicon.ico", "/smartum.jpg").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        // після входу: тимчасовий пароль — на зміну пароля, інакше на головну (LoginSuccessHandler)
                        .successHandler(loginSuccessHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    // Паролі в базі — тільки хеш з префіксом алгоритму: {bcrypt}$2a$10$...
    // Нові паролі хешуються BCrypt; префікс каже, чим перевіряти вже збережений пароль
    // (старий {noop} теж розпізнається — щоб ніхто не втратив доступ під час переходу).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
