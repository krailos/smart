package com.krailo.smart.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.krailo.smart.config.LoginSuccessHandler.CHANGE_PASSWORD_URL;
import static com.krailo.smart.config.LoginSuccessHandler.MUST_CHANGE_PASSWORD;

// Спрацьовує перед КОЖНОЮ сторінкою: поки пароль тимчасовий (позначка в сесії від LoginSuccessHandler),
// будь-яка адреса веде на "Змінити мій пароль". Сама ця сторінка і вихід — дозволені (див. WebConfiguration).
public class MustChangePasswordInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute(MUST_CHANGE_PASSWORD))) {
            response.sendRedirect(request.getContextPath() + CHANGE_PASSWORD_URL);
            return false;   // сторінку, яку просили, не показуємо
        }
        return true;
    }
}
