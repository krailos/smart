package com.krailo.smart.repository;


import com.krailo.smart.entity.Audience;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AudienceRepositoryTest {
    @Autowired
    private TestEntityManager entityManager; // Допоміжний клас для тестів JPA

    @Autowired
    private AudienceRepository audienceRepository;

    @Test
    void shouldSaveAndGenerateIdCorrectly() {
        // Given (Підготовка)
        Audience testAudience = Audience.builder().name("testName").description("test description").build();

        // When (Дія)
        Audience savedAudience = audienceRepository.saveAndFlush(testAudience);

        // Then (Перевірки AssertJ для сутностей та об'єктів)
        assertThat(savedAudience).isNotNull(); // Перевірка, що об'єкт взагалі існує

        // Перевіряємо генерацію ID базою даних (IDENTITY)
        assertThat(savedAudience.getId())
                .isNotNull()
                .isPositive();


        // Перевірка конкретних полів за допомогою вбудованого методу AssertJ
        assertThat(savedAudience)
                .hasFieldOrPropertyWithValue("name", "testName")
                .hasFieldOrPropertyWithValue("description", "test description");
    }

    @Test
    void shouldFindAudienceByIdAndCompareObjects() {
        // Given
        Audience original = Audience.builder().name("testName").description("test description").build();
        entityManager.persistAndFlush(original); // Зберігаємо в тестову БД

        // When
        Optional <Audience> foundOptional = audienceRepository.findById(original.getId());

        // Then
        assertThat(foundOptional).isPresent(); // Перевірка Optional

        Audience actual = foundOptional.get();

        // СУПЕР-МЕТОД: Порівняння двох об'єктів за значеннями всіх їхніх полів (recursive comparison).
        // Це ідеально, адже звичайний actual.equals(original) може видати false,
        // якщо у вас не перевизначений метод equals() в Entity.
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    @Test
    void shouldFindAudienceAndVerifyListContent() {
        // Given
        Audience testAudience1 = Audience.builder().name("testName1").description("test description 1").build();
        Audience testAudience2 = Audience.builder().name("testName2").description("test description 2").build();
        Audience testAudience3 = Audience.builder().name("testName3").description("test description 3").build();


        entityManager.persist(testAudience1);
        entityManager.persist(testAudience2);
        entityManager.persist(testAudience3);
        entityManager.flush();

        // When
        List<Audience> result = audienceRepository.findAll();

        // Then (Перевірка колекцій/списків сутностей)
        assertThat(result)
                .isNotEmpty()
                .hasSize(3) // Має знайти тільки 2 аудиторії (50 і 100 місць)
                // Витягуємо тільки імена з отриманих об'єктів для простої перевірки рядків
                .extracting(Audience::getName)
                .containsExactlyInAnyOrder("testName1", "testName2", "testName3")
                .doesNotContain("testName4");
    }



}
