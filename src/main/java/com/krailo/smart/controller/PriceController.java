package com.krailo.smart.controller;

import com.krailo.smart.dto.PriceDto;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("prices", priceService.findAll());
        return "prices";
    }

    // ціни одного курсу і форма нової ціни — у SubjectController: /subjects/{id}/prices, /subjects/{id}/prices/new

    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
          PriceDto priceDto =  priceService.findById(id);
          model.addAttribute("price", priceDto);
        return "price";
    }

    @PostMapping("/create")
    public String create ( @ModelAttribute PriceDto priceDto, RedirectAttributes redirectAttributes) {
        try {
            priceService.create(priceDto);
        } catch (BusinessException e) {
            // ціна курсу на цю дату вже є — показуємо повідомлення на сторінці курсів
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/subjects";
    }
    
    
    @PostMapping("/{id}/update")
    public String update (@PathVariable("id") Integer id, @ModelAttribute PriceDto price,
                          RedirectAttributes redirectAttributes) {
        try {
            priceService.update(id, price);
        } catch (BusinessException e) {
            // неправильна вартість (порожня, від'ємна, зайві знаки) — показуємо повідомлення на сторінці цін
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/prices";
    }
    
    @PostMapping("/{id}/delete")
    public String delete (@PathVariable("id") Integer id) {
         priceService.delete(id); 
        return "redirect:/prices";        
    }

}
