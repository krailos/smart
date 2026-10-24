package com.krailo.smart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// профіль test — база H2 у пам'яті; без нього тест підключався до справжньої PostgreSQL
// і Flyway застосовував нові міграції просто під час запуску тестів
@SpringBootTest
@ActiveProfiles("test")
class SmartApplicationTests {


    @Test
    void contextLoads() {
    }

}
