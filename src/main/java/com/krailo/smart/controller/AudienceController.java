package com.krailo.smart.controller;


import com.krailo.smart.dto.AudienceDto;
import com.krailo.smart.entity.Audience;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.AudienceService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        model.addAttribute("audience", new Audience());
        return "audienceNew";
    }


    @PostMapping("/create")
    // @ResponseStatus(HttpStatus.CREATED)
    public String create ( @ModelAttribute AudienceDto audience) {
        audienceService.create(audience);
        return "redirect:/audiences";
    }

    @PostMapping("/{id}/update")
    public String update (@PathVariable("id") Integer id, @ModelAttribute AudienceDto audience) {
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
