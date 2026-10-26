package com.krailo.smart.controller;

import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

import static com.krailo.smart.config.LoginSuccessHandler.CHANGE_PASSWORD_URL;
import static com.krailo.smart.config.LoginSuccessHandler.MUST_CHANGE_PASSWORD;

// "Мій обліковий запис": кожен користувач змінює СВІЙ пароль (чий — беремо з Principal, а не з форми)
@Controller
@AllArgsConstructor
public class AccountController {

    private final UserService userService;

    @GetMapping(CHANGE_PASSWORD_URL)
    public String passwordForm(HttpSession session, Model model) {
        // прийшов сюди примусово — пароль тимчасовий (див. LoginSuccessHandler)
        model.addAttribute("forced", Boolean.TRUE.equals(session.getAttribute(MUST_CHANGE_PASSWORD)));
        model.addAttribute("minPasswordLength", UserService.MIN_PASSWORD_LENGTH);
        return "accountPassword";
    }

    @PostMapping(CHANGE_PASSWORD_URL)
    public String changePassword(@RequestParam(required = false) String currentPassword,
                                 @RequestParam(required = false) String newPassword,
                                 @RequestParam(required = false) String repeatPassword,
                                 Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            userService.changeOwnPassword(principal.getName(), currentPassword, newPassword, repeatPassword);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:" + CHANGE_PASSWORD_URL;
        }
        // пароль уже свій — знімаємо "замок" з інших сторінок
        session.removeAttribute(MUST_CHANGE_PASSWORD);
        redirectAttributes.addFlashAttribute("successMessage", "Пароль змінено");
        return "redirect:/";
    }
}
