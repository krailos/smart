package com.krailo.smart.controller;

import com.krailo.smart.dto.DiscountDto;
import com.krailo.smart.entity.Audience;
import com.krailo.smart.service.DiscountService;
import com.krailo.smart.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/discounts")
@AllArgsConstructor
public class DiscountController {

    private DiscountService discountService;
    private StudentService studentService;

    // /discounts — активні, /discounts?archived=true — архів (той самий шаблон)
    @GetMapping
    public String findAll(@RequestParam(defaultValue = "false") boolean archived, Model model) {
        model.addAttribute("discounts", archived ? discountService.findAllArchived() : discountService.findAll());
        model.addAttribute("archived", archived);
        return "discounts";
    }


    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("discount", discountService.findById(id));
        return "discount";
    }


    @GetMapping("/discount/new")
    public String createForm(Model model) {
        model.addAttribute("audience", new Audience());
        return "discountNew";
    }


    @PostMapping("/create")
    // @ResponseStatus(HttpStatus.CREATED)
    public String create(@ModelAttribute DiscountDto discount) {
        discountService.create(discount);
        return "redirect:/discounts";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Integer id, @ModelAttribute DiscountDto discount) {
        discountService.update(id, discount);
        return "redirect:/discounts";
    }

    // архівування замість видалення (див. DiscountService.archive)
    @PostMapping("/{id}/archive")
    public String archive(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        discountService.archive(id);
        redirectAttributes.addFlashAttribute("successMessage", "Знижку переміщено в архів");
        return "redirect:/discounts";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        discountService.restore(id);
        redirectAttributes.addFlashAttribute("successMessage", "Знижку відновлено з архіву");
        return "redirect:/discounts?archived=true";
    }

}
