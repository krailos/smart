package com.krailo.smart.config;

import com.krailo.smart.enumeration.Role;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
        // Хто куди може: правила перевіряються зверху вниз, спрацьовує перше, що підійшло
        http.authorizeHttpRequests(auth -> auth
                        // сторінка помилки (404/403/500) — показуємо будь-кому, інакше помилка "ховається" за іншою
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        // статичні файли (стилі, іконка, фон головної) — без логіну, щоб сторінка входу мала оформлення
                        .requestMatchers("/css/**", "/favicon.ico", "/smartum-3.jpg").permitAll()
                        // вчитель (і адмін): головна, свій розклад-список, свої уроки, свої учні, свій пароль, проведення уроку
                        // ("своє" відбирають контролери — див. CurrentUser)
                        .requestMatchers(HttpMethod.GET, "/", "/schedules", "/lessons", "/my/students",
                                "/schedules/*/lesson/new").authenticated()
                        .requestMatchers(HttpMethod.POST, "/lessons/create").authenticated()
                        .requestMatchers(LoginSuccessHandler.CHANGE_PASSWORD_URL).authenticated()
                        // усе інше — тільки адмін (вчителю -> 403 "Доступ заборонено")
                        .anyRequest().hasAuthority(Role.ADMIN.getAuthority()))
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
