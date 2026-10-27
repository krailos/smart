package com.krailo.smart.service;

import com.krailo.smart.entity.Teacher;
import com.krailo.smart.entity.User;
import com.krailo.smart.enumeration.Role;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.repository.TeacherRepository;
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

    @Autowired private TeacherRepository teacherRepository;

    private User admin;
    private Teacher teacher;

    @BeforeEach
    void setUp() {
        // той, хто працює зі сторінкою користувачів
        admin = userRepository.save(User.builder().name("admin").password(passwordEncoder.encode("admin-pass")).role(Role.ADMIN).build());
        // картка вчителя, з якою пов'язують логін ролі «Вчитель»
        teacher = teacherRepository.save(Teacher.builder().firstName("Анна").lastName("Коваль").build());
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();     // спершу логіни — вони посилаються на вчителів
        teacherRepository.deleteAll();
    }

    @Test
    void shouldAcceptLoginOf50Chars_ButNot51() {
        // межа — саме там, де найчастіше помиляються "на одиницю" (колонка users.name — varchar(50))
        assertThatThrownBy(() -> userService.create("a".repeat(51), "temp-pass-1", Role.ADMIN, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("задовгий");

        userService.create("a".repeat(50), "temp-pass-1", Role.ADMIN, null);
        assertThat(userRepository.findUserByNameIgnoreCaseAndArchivedFalse("a".repeat(50))).isPresent();
    }

    @Test
    void shouldIgnoreLoginCase() {
        // Given: адмін створив "Olena"
        userService.create("Olena", "temp-pass-1", Role.ADMIN, null);

        // Then: увійти можна з будь-якими великими/малими літерами,
        // а далі в застосунку логін такий, як у базі ("Olena")
        assertThat(login("OLENA", "temp-pass-1").getName()).isEqualTo("Olena");
        assertThat(login("olena", "temp-pass-1").getName()).isEqualTo("Olena");

        // другу "olena" створити не можна — це той самий логін
        assertThatThrownBy(() -> userService.create("olena", "temp-pass-1", Role.ADMIN, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("великими/малими літерами");
    }

    @Test
    void shouldLinkTeacherLogin_OnlyToOneActiveTeacher() {
        // вчитель без картки — не можна; адмін — без картки, навіть якщо її передали
        assertThatThrownBy(() -> userService.create("anna", "temp-pass-1", Role.TEACHER, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("оберіть, хто це з вчителів");
        userService.create("boss", "temp-pass-1", Role.ADMIN, teacher.getId());
        assertThat(userRepository.findUserByNameIgnoreCaseAndArchivedFalse("boss").orElseThrow().getTeacher()).isNull();

        // When: логін вчителя
        userService.create("anna", "temp-pass-1", Role.TEACHER, teacher.getId());

        // Then: пов'язаний з карткою; другий логін тій самій вчительці — не можна
        assertThat(userRepository.findUserByNameIgnoreCaseAndArchivedFalse("anna").orElseThrow().getTeacher().getId())
                .isEqualTo(teacher.getId());
        assertThatThrownBy(() -> userService.create("anna2", "temp-pass-1", Role.TEACHER, teacher.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("уже є логін «anna»");

        // вчителя в архіві — не можна
        Teacher archived = teacherRepository.save(Teacher.builder().firstName("Іван").lastName("Сірко").archived(true).build());
        assertThatThrownBy(() -> userService.create("ivan", "temp-pass-1", Role.TEACHER, archived.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("в архіві");

        // anna в архіві — картка вільна для нового логіна; тоді відновити anna вже не можна
        Integer annaId = userRepository.findUserByNameIgnoreCaseAndArchivedFalse("anna").orElseThrow().getId();
        userService.archive(annaId, "admin");
        userService.create("anna2", "temp-pass-1", Role.TEACHER, teacher.getId());
        assertThatThrownBy(() -> userService.restore(annaId))
                .isInstanceOf(BusinessException.class).hasMessageContaining("уже є логін «anna2»");

        // адмін -> вчитель (через зміну ролі): треба обрати вчителя; вчитель -> адмін: прив'язка знімається
        Integer bossId = userRepository.findUserByNameIgnoreCaseAndArchivedFalse("boss").orElseThrow().getId();
        assertThatThrownBy(() -> userService.changeRole(bossId, Role.TEACHER, null, "admin"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("оберіть, хто це з вчителів");
        Integer anna2Id = userRepository.findUserByNameIgnoreCaseAndArchivedFalse("anna2").orElseThrow().getId();
        userService.changeRole(anna2Id, Role.ADMIN, teacher.getId(), "admin");
        assertThat(userRepository.findById(anna2Id).orElseThrow().getTeacher()).isNull();
    }

    @Test
    void shouldCreateUserWithHashedPassword() {
        // When
        userService.create("  anna ", "temp-pass-1", Role.TEACHER, teacher.getId());

        // Then: логін без пробілів по краях, у базі — хеш, з тимчасовим паролем можна увійти
        User anna = userRepository.findUserByNameIgnoreCaseAndArchivedFalse("anna").orElseThrow();
        assertThat(anna.getPassword()).startsWith("{bcrypt}").doesNotContain("temp-pass-1");
        assertThat(login("anna", "temp-pass-1").isAuthenticated()).isTrue();
    }

    @Test
    void shouldRejectBadNewUser() {
        assertThatThrownBy(() -> userService.create("anna", "short", Role.TEACHER, teacher.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("не коротший за 8");
        assertThatThrownBy(() -> userService.create(" ", "temp-pass-1", Role.TEACHER, teacher.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Вкажіть логін");
        assertThatThrownBy(() -> userService.create("anna maria", "temp-pass-1", Role.TEACHER, teacher.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("без пробілів");
        assertThatThrownBy(() -> userService.create("anna", "temp-pass-1", null, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Оберіть роль");
        // такий логін уже є
        assertThatThrownBy(() -> userService.create("admin", "temp-pass-1", Role.TEACHER, teacher.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("уже є");

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldResetPassword_OldOneStopsWorking() {
        // Given
        userService.create("anna", "temp-pass-1", Role.TEACHER, teacher.getId());
        Integer id = userRepository.findUserByNameIgnoreCaseAndArchivedFalse("anna").orElseThrow().getId();

        userService.changeOwnPassword("anna", "temp-pass-1", "anna-own-pass", "anna-own-pass");
        assertThat(userService.mustChangePassword("anna")).isFalse();

        // When: "забула пароль" — адмін задає новий
        userService.resetPassword(id, "new-pass-22", "admin");

        // Then: новий працює, старий ні, і він знову тимчасовий
        assertThat(login("anna", "new-pass-22").isAuthenticated()).isTrue();
        assertThatThrownBy(() -> login("anna", "anna-own-pass")).isInstanceOf(BadCredentialsException.class);
        assertThat(userService.mustChangePassword("anna")).isTrue();
        assertThatThrownBy(() -> userService.resetPassword(id, "1234567", "admin"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("не коротший за 8");
        // свій пароль так не скинути — тільки через "Змінити мій пароль" (з поточним паролем)
        assertThatThrownBy(() -> userService.resetPassword(admin.getId(), "new-pass-22", "admin"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Змінити мій пароль");
    }

    @Test
    void shouldMarkNewUserPasswordAsTemporary_UntilChangedByUser() {
        // Given: адмін створив користувача — пароль тимчасовий
        userService.create("anna", "temp-pass-1", Role.TEACHER, teacher.getId());
        assertThat(userService.mustChangePassword("anna")).isTrue();

        // When: анна задає свій
        userService.changeOwnPassword("anna", "temp-pass-1", "anna-own-pass", "anna-own-pass");

        // Then: позначки немає, входить уже зі своїм паролем
        assertThat(userService.mustChangePassword("anna")).isFalse();
        assertThat(login("anna", "anna-own-pass").isAuthenticated()).isTrue();
        assertThatThrownBy(() -> login("anna", "temp-pass-1")).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRejectBadOwnPasswordChange() {
        userService.create("anna", "temp-pass-1", Role.TEACHER, teacher.getId());

        assertThatThrownBy(() -> userService.changeOwnPassword("anna", "wrong-pass", "anna-own-pass", "anna-own-pass"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Поточний пароль");
        assertThatThrownBy(() -> userService.changeOwnPassword("anna", "temp-pass-1", "anna-own-pass", "anna-own-pazz"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("не збігаються");
        assertThatThrownBy(() -> userService.changeOwnPassword("anna", "temp-pass-1", "short", "short"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("не коротший за 8");
        assertThatThrownBy(() -> userService.changeOwnPassword("anna", "temp-pass-1", "temp-pass-1", "temp-pass-1"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("відрізнятися");

        // нічого не змінилось: пароль той самий і досі тимчасовий
        assertThat(login("anna", "temp-pass-1").isAuthenticated()).isTrue();
        assertThat(userService.mustChangePassword("anna")).isTrue();
    }

    @Test
    void shouldNotLogin_WhenArchived_AndLoginAgainAfterRestore() {
        // Given
        userService.create("anna", "temp-pass-1", Role.TEACHER, teacher.getId());
        Integer id = userRepository.findUserByNameIgnoreCaseAndArchivedFalse("anna").orElseThrow().getId();

        // When: в архів — увійти не може (Spring каже "невірний логін або пароль")
        userService.archive(id, "admin");
        assertThatThrownBy(() -> login("anna", "temp-pass-1")).isInstanceOf(BadCredentialsException.class);

        // логін в архіві теж зайнятий — друга "anna" не створиться
        assertThatThrownBy(() -> userService.create("anna", "temp-pass-1", Role.TEACHER, teacher.getId()))
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
        assertThatThrownBy(() -> userService.changeRole(admin.getId(), Role.TEACHER, teacher.getId(), "admin"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Свою роль");

        // admin — єдиний адмін; навіть хтось інший (наприклад, вчитель) не може його прибрати
        assertThatThrownBy(() -> userService.archive(admin.getId(), "anna"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("останній адміністратор");
        assertThatThrownBy(() -> userService.changeRole(admin.getId(), Role.TEACHER, teacher.getId(), "anna"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("останній адміністратор");

        // з'явився другий адмін — тепер першого можна зробити вчителем
        userService.create("boss", "temp-pass-1", Role.ADMIN, null);
        userService.changeRole(admin.getId(), Role.TEACHER, teacher.getId(), "boss");
        assertThat(userRepository.findById(admin.getId()).orElseThrow().getRole()).isEqualTo(Role.TEACHER);
    }

    private Authentication login(String name, String password) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider.authenticate(new UsernamePasswordAuthenticationToken(name, password));
    }
}
