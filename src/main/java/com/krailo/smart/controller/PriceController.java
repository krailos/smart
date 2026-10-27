package com.krailo.smart.controller;

import com.krailo.smart.dto.PriceDto;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.PriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

    // @Valid + BindingResult: неправильні поля — та сама форма з текстом біля поля (див. AudienceController)
    @PostMapping("/create")
    public String create (@Valid @ModelAttribute("price") PriceDto priceDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "priceNew";
        }
        try {
            priceService.create(priceDto);
        } catch (BusinessException e) {
            // ціна курсу на цю дату вже є — текст біля поля «Діє з»
            bindingResult.rejectValue("date", "duplicate", e.getMessage());
            return "priceNew";
        }
        return "redirect:/subjects";
    }


    @PostMapping("/{id}/update")
    public String update (@PathVariable("id") Integer id, @Valid @ModelAttribute("price") PriceDto price,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "price";
        }
        priceService.update(id, price);
        return "redirect:/prices";
    }
    
    @PostMapping("/{id}/delete")
    public String delete (@PathVariable("id") Integer id) {
         priceService.delete(id); 
        return "redirect:/prices";        
    }

}
