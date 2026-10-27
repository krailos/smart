package com.krailo.smart.controller;


import com.krailo.smart.dto.AudienceDto;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.AudienceService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/audiences")
@AllArgsConstructor
public class AudienceController {

    private final AudienceService audienceService;

    // /audiences — активні, /audiences?archived=true — архів (той самий шаблон)
    @GetMapping
    public String findAll(@RequestParam(defaultValue = "false") boolean archived, Model model) {
        model.addAttribute("audiences", archived ? audienceService.findAllArchived() : audienceService.findAll());
        model.addAttribute("archived", archived);
        return "audiences";
    }



    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
        model.addAttribute("audience", audienceService.findById(id));
        return "audience";
    }


    @GetMapping("/new")
    public String createForm (Model model) {
        model.addAttribute("audience", new AudienceDto());
        return "audienceNew";
    }


    @PostMapping("/create")
    // @ResponseStatus(HttpStatus.CREATED)
    // @Valid + BindingResult — як у формі оплати: порожні/задовгі поля — та сама форма з текстом біля поля,
    // а не 500 чи запис без назви в базі
    public String create (@Valid @ModelAttribute("audience") AudienceDto audience, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "audienceNew";
        }
        audienceService.create(audience);
        return "redirect:/audiences";
    }

    // {id} з адреси Spring сам підставляє в audience.id — після помилки форма знову відправиться на /audiences/{id}/update
    @PostMapping("/{id}/update")
    public String update (@PathVariable("id") Integer id, @Valid @ModelAttribute("audience") AudienceDto audience,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "audience";
        }
        audienceService.update(id, audience);
        return "redirect:/audiences";
    }

    // архівування замість видалення (див. AudienceService.archive)
    @PostMapping("/{id}/archive")
    public String archive (@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            audienceService.archive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Аудиторію переміщено в архів");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/audiences";
    }

    @PostMapping("/{id}/restore")
    public String restore (@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        audienceService.restore(id);
        redirectAttributes.addFlashAttribute("successMessage", "Аудиторію відновлено з архіву");
        return "redirect:/audiences?archived=true";
    }

}
