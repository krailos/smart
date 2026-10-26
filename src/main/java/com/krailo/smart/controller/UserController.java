package com.krailo.smart.controller;


import com.krailo.smart.enumeration.Role;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.TeacherService;
import com.krailo.smart.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

// Керування користувачами. Principal — той, хто зараз увійшов (Spring підставляє сам):
// за ним перевіряємо "не можна архівувати себе / змінити собі роль"
@Controller
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    UserService userService;
    TeacherService teacherService;

    // /users — активні, /users?archived=true — архів (той самий шаблон)
    @GetMapping
    public String findAll(@RequestParam(defaultValue = "false") boolean archived, Model model, Principal principal) {
        model.addAttribute("users", userService.findAll(archived));
        model.addAttribute("archived", archived);
        model.addAttribute("currentUser", principal.getName());
        model.addAttribute("roles", Role.values());
        model.addAttribute("teachers", teacherService.findAll());   // активні — для ролі «Вчитель»
        model.addAttribute("minPasswordLength", UserService.MIN_PASSWORD_LENGTH);
        return "users";
    }

    @PostMapping("/create")
    public String create(@RequestParam(required = false) String name,
                         @RequestParam(required = false) String password,
                         @RequestParam(required = false) Role role,
                         @RequestParam(required = false) Integer teacherId,
                         RedirectAttributes redirectAttributes) {
        try {
            userService.create(name, password, role, teacherId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Користувача «" + name.trim() + "» додано. Передайте йому тимчасовий пароль");
        } catch (BusinessException e) {
            // повертаємо форму з введеним логіном і роллю; пароль назад на сторінку не віддаємо
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("draftName", name);
            redirectAttributes.addFlashAttribute("draftRole", role);
            redirectAttributes.addFlashAttribute("draftTeacherId", teacherId);
            redirectAttributes.addFlashAttribute("openForm", "create");
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable("id") Integer id, @RequestParam(required = false) Role role,
                             @RequestParam(required = false) Integer teacherId,
                             Principal principal, RedirectAttributes redirectAttributes) {
        try {
            userService.changeRole(id, role, teacherId, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Роль змінено");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/password")
    public String resetPassword(@PathVariable("id") Integer id, @RequestParam(required = false) String password,
                                Principal principal, RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(id, password, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пароль змінено. Передайте його користувачу — при вході він задасть свій");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable("id") Integer id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            userService.archive(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Користувача переміщено в архів — увійти він більше не може");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            userService.restore(id);
            redirectAttributes.addFlashAttribute("successMessage", "Користувача відновлено з архіву");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users?archived=true";
    }
}
