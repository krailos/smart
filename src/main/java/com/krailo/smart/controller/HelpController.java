package com.krailo.smart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Довідка адміністратора: як вести облік у програмі (лише ADMIN — як і решта сторінок, не відкритих вчителю).
// Текст — у шаблоні help.html; змінилась програма — оновлюємо й довідку (копія — Google Doc «Smartum — інструкція адміністратора»)
@Controller
public class HelpController {

    @GetMapping("/help")
    public String help() {
        return "help";
    }

}
