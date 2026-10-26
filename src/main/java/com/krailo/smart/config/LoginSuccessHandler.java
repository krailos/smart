package com.krailo.smart.config;

import com.krailo.smart.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Що робити одразу після правильного логіна й пароля.
// Тимчасовий пароль (задав адмін) — позначка в сесії і перехід на "Змінити мій пароль";
// далі MustChangePasswordInterceptor не пустить на інші сторінки, поки пароль не змінено.
// Інакше — як звичайно: туди, куди людина йшла до входу, або на головну.
@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public static final String MUST_CHANGE_PASSWORD = "mustChangePassword";
    public static final String CHANGE_PASSWORD_URL = "/account/password";

    private final UserService userService;

    public LoginSuccessHandler(UserService userService) {
        this.userService = userService;
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (userService.mustChangePassword(authentication.getName())) {
            request.getSession().setAttribute(MUST_CHANGE_PASSWORD, true);
            getRedirectStrategy().sendRedirect(request, response, CHANGE_PASSWORD_URL);
            return;
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
