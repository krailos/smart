package com.krailo.smart.controller;

import com.krailo.smart.dto.DiscountDto;
import com.krailo.smart.entity.Audience;
import com.krailo.smart.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("discounts", discountService.findAll());
        return "/discounts";
    }
    
    
    
    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
          model.addAttribute("discount", discountService.findById(id));
        return "/discount";        
    }
    
    
    @GetMapping("/discount/new")
    public String createForm (Model model) {
    model.addAttribute("audience", new Audience());
    return "/discountNew";
    }
    
    
    @PostMapping("/create")
   // @ResponseStatus(HttpStatus.CREATED)
    public String create ( @ModelAttribute DiscountDto discount) {
        discountService.create(discount);  
        return "redirect:/discounts";     
    }
    
    @PostMapping("/{id}/update")
    public String update (@PathVariable("id") Integer id, @ModelAttribute DiscountDto discount) {
           discountService.update(id, discount); 
        return "redirect:/discounts";        
    }
    
    @PostMapping("/{id}/delete")
    public String delete (@PathVariable("id") Integer id) {
         discountService.delete(id); 
        return "redirect:/discounts";        
    }

}
