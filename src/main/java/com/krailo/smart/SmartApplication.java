package com.krailo.smart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

@SpringBootApplication
public class SmartApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SmartApplication.class);
        app.addListeners(requireDbPassword());
        app.run(args);
    }

    // Налаштування зібрано, до бази ще не підключались: немає пароля — зупиняємось із зрозумілим поясненням.
    // (Без цього Spring передав би в базу буквально текст "${DB_PASSWORD}" і впав би з "password authentication failed")
    private static ApplicationListener<ApplicationEnvironmentPreparedEvent> requireDbPassword() {
        return event -> {
            if (!event.getEnvironment().containsProperty("DB_PASSWORD")) {
                throw new IllegalStateException("Не задано пароль до бази DB_PASSWORD. "
                        + "Створіть файл secrets.properties поруч з pom.xml (зразок — secrets.example.properties) "
                        + "або задайте змінну середовища DB_PASSWORD");
            }
        };
    }
}
