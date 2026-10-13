package com.krailo.smart.controller;

import com.krailo.smart.dto.PriceDto;
import com.krailo.smart.dto.SubjectDto;
import com.krailo.smart.service.PriceService;
import com.krailo.smart.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;
    private final SubjectService subjectService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("prices", priceService.findAll());
        return "prices";
    }
    
    @GetMapping("/{id}/bysubject")
    public String findBySubject( @PathVariable("id") Integer id, Model model) {
        SubjectDto subjectDto = subjectService.findById(id);
        model.addAttribute("subject", subjectDto);
        model.addAttribute("prices", subjectDto.getPricesDto());
        return "prices";
    }
    
    
    
    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
          PriceDto priceDto =  priceService.findById(id);
          model.addAttribute("price", priceDto);
        return "price";
    }
    
    
//    @GetMapping("/price/new")
//    public String createForm (Model model) {
//    model.addAttribute("subjects", subjectService.findAll());
//    return "priceNew";
//    }
    
    @GetMapping("/price/new/{id}")
    public String createFormWhithChackedSubject (@PathVariable("id") Integer subjectId, Model model) {
        SubjectDto subjectDto =  subjectService.findById(subjectId);
        PriceDto priceDto = PriceDto.builder()
                .subjectId(subjectDto.getId())
                .subjectName(subjectDto.getName())
                .build();
    model.addAttribute("price", priceDto);
    return "priceNew";
    }
    
    
    @PostMapping("/create")
    public String create ( @ModelAttribute PriceDto priceDto) {
        System.out.println(priceDto);
        priceService.create(priceDto);  
        return "redirect:/subjects";
    }
    
    
    @PostMapping("/{id}/update")
    public String update (@PathVariable("id") Integer id, @ModelAttribute PriceDto price) {
        System.out.println(price);
           priceService.update(id, price); 
        return "redirect:/prices";        
    }
    
    @PostMapping("/{id}/delete")
    public String delete (@PathVariable("id") Integer id) {
         priceService.delete(id); 
        return "redirect:/prices";        
    }

}
