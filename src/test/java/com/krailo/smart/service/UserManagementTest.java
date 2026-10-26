package com.krailo.smart.service;

import com.krailo.smart.entity.User;
import com.krailo.smart.enumeration.Role;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

// Керування користувачами: додати, роль, новий пароль, архів — і захист від помилок
@SpringBootTest
@ActiveProfiles("test")
class UserManagementTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User admin;

    @BeforeEach
    void setUp() {
        // той, хто працює зі сторінкою користувачів
        admin = userRepository.save(User.builder().name("admin").password(passwordEncoder.encode("admin-pass")).role(Role.ADMIN).build());
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUserWithHashedPassword() {
        // When
        userService.create("  anna ", "temp-pass-1", Role.TEACHER);

        // Then: логін без пробілів по краях, у базі — хеш, з тимчасовим паролем можна увійти
        User anna = userRepository.findUserByNameAndArchivedFalse("anna").orElseThrow();
        assertThat(anna.getPassword()).startsWith("{bcrypt}").doesNotContain("temp-pass-1");
        assertThat(login("anna", "temp-pass-1").isAuthenticated()).isTrue();
    }

    @Test
    void shouldRejectBadNewUser() {
        assertThatThrownBy(() -> userService.create("anna", "short", Role.TEACHER))
                .isInstanceOf(BusinessException.class).hasMessageContaining("не коротший за 8");
        assertThatThrownBy(() -> userService.create(" ", "temp-pass-1", Role.TEACHER))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Вкажіть логін");
        assertThatThrownBy(() -> userService.create("anna maria", "temp-pass-1", Role.TEACHER))
                .isInstanceOf(BusinessException.class).hasMessageContaining("без пробілів");
        assertThatThrownBy(() -> userService.create("anna", "temp-pass-1", null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Оберіть роль");
        // такий логін уже є
        assertThatThrownBy(() -> userService.create("admin", "temp-pass-1", Role.TEACHER))
                .isInstanceOf(BusinessException.class).hasMessageContaining("уже є");

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldResetPassword_OldOneStopsWorking() {
        // Given
        userService.create("anna", "temp-pass-1", Role.TEACHER);
        Integer id = userRepository.findUserByNameAndArchivedFalse("anna").orElseThrow().getId();

        // When: "забула пароль" — адмін задає новий
        userService.resetPassword(id, "new-pass-22");

        // Then
        assertThat(login("anna", "new-pass-22").isAuthenticated()).isTrue();
        assertThatThrownBy(() -> login("anna", "temp-pass-1")).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> userService.resetPassword(id, "1234567"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("не коротший за 8");
    }

    @Test
    void shouldNotLogin_WhenArchived_AndLoginAgainAfterRestore() {
        // Given
        userService.create("anna", "temp-pass-1", Role.TEACHER);
        Integer id = userRepository.findUserByNameAndArchivedFalse("anna").orElseThrow().getId();

        // When: в архів — увійти не може (Spring каже "невірний логін або пароль")
        userService.archive(id, "admin");
        assertThatThrownBy(() -> login("anna", "temp-pass-1")).isInstanceOf(BadCredentialsException.class);

        // логін в архіві теж зайнятий — друга "anna" не створиться
        assertThatThrownBy(() -> userService.create("anna", "temp-pass-1", Role.TEACHER))
                .isInstanceOf(BusinessException.class).hasMessageContaining("в архіві");

        // Then: відновили — знову входить
        userService.restore(id);
        assertThat(login("anna", "temp-pass-1").isAuthenticated()).isTrue();
    }

    @Test
    void shouldProtectSelfAndLastAdmin() {
        // себе — ні в архів, ні змінити роль
        assertThatThrownBy(() -> userService.archive(admin.getId(), "admin"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Себе");
        assertThatThrownBy(() -> userService.changeRole(admin.getId(), Role.TEACHER, "admin"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Свою роль");

        // admin — єдиний адмін; навіть хтось інший (наприклад, вчитель) не може його прибрати
        assertThatThrownBy(() -> userService.archive(admin.getId(), "anna"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("останній адміністратор");
        assertThatThrownBy(() -> userService.changeRole(admin.getId(), Role.TEACHER, "anna"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("останній адміністратор");

        // з'явився другий адмін — тепер першого можна зробити вчителем
        userService.create("boss", "temp-pass-1", Role.ADMIN);
        userService.changeRole(admin.getId(), Role.TEACHER, "boss");
        assertThat(userRepository.findById(admin.getId()).orElseThrow().getRole()).isEqualTo(Role.TEACHER);
    }

    private Authentication login(String name, String password) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider.authenticate(new UsernamePasswordAuthenticationToken(name, password));
    }
}
