package com.krailo.smart.config;

import com.krailo.smart.entity.User;
import com.krailo.smart.enumeration.Role;
import com.krailo.smart.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Хто зараз працює із застосунком. Spring тримає того, хто увійшов, у SecurityContextHolder;
// звідти — логін і ролі, а за логіном — картка вчителя (users.teacher_id).
// Адмін бачить усе; вчитель — тільки "своє": заняття, які він веде, їхні уроки та учнів.
@Component
@AllArgsConstructor
public class CurrentUser {

    private final UserRepository userRepository;

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> Role.ADMIN.getAuthority().equals(a.getAuthority()));
    }

    // id картки вчителя; null — у адміна або якщо вчителя не обрано (тоді вчитель нічого не бачить)
    @Transactional(readOnly = true)
    public Integer teacherId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return userRepository.findUserByNameAndArchivedFalse(auth.getName())
                .map(User::getTeacher)
                .map(teacher -> teacher.getId())
                .orElse(null);
    }

    // вчитель може працювати тільки зі своїм заняттям розкладу (адміну можна все)
    public void requireOwnSchedule(Integer scheduleTeacherId) {
        if (isAdmin()) {
            return;
        }
        Integer me = teacherId();
        if (me == null || !me.equals(scheduleTeacherId)) {
            throw new AccessDeniedException("Це заняття веде інший вчитель");
        }
    }
}
