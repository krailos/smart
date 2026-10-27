package com.krailo.smart.controller;

import com.krailo.smart.dto.PriceDto;
import com.krailo.smart.dto.SubjectDto;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    // /subjects — активні, /subjects?archived=true — архів (той самий шаблон)
    @GetMapping
    public String findAll(@RequestParam(defaultValue = "false") boolean archived, Model model) {
        model.addAttribute("subjects", archived ? subjectService.findAllArchived() : subjectService.findAll());
        model.addAttribute("archived", archived);
        return "subjects";
    }
    
    
    
    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
          model.addAttribute("subject", subjectService.findById(id));
        return "subject";
    }

    // історія цін курсу (була /prices/{id}/bysubject): ціни належать курсу — адреса від власника
    @GetMapping("/{id}/prices")
    public String prices(@PathVariable("id") Integer id, Model model) {
        SubjectDto subjectDto = subjectService.findById(id);
        model.addAttribute("subject", subjectDto);
        model.addAttribute("prices", subjectDto.getPricesDto());
        return "prices";
    }

    // форма нової ціни для курсу (була /prices/price/new/{id})
    @GetMapping("/{id}/prices/new")
    public String newPriceForm(@PathVariable("id") Integer id, Model model) {
        SubjectDto subjectDto = subjectService.findById(id);
        model.addAttribute("price", PriceDto.builder()
                .subjectId(subjectDto.getId())
                .subjectName(subjectDto.getName())
                .build());
        return "priceNew";
    }
    
    
    @GetMapping("/subject/new")
    public String createForm (Model model) {
    model.addAttribute("subject", new Subject());
    return "subjectNew";
    }
    
    
    @PostMapping("/create")
   // @ResponseStatus(HttpStatus.CREATED)
    public String create ( @ModelAttribute SubjectDto subject) {
        subjectService.create(subject);  
        return "redirect:/subjects";     
    }
    
    @PostMapping("/{id}/update")
    public String update (@PathVariable("id") Integer id, @ModelAttribute SubjectDto subject) {
           subjectService.update(id, subject); 
        return "redirect:/subjects";        
    }
    
    // архівування замість видалення (див. SubjectService.archive)
    @PostMapping("/{id}/archive")
    public String archive (@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            subjectService.archive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Курс переміщено в архів");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/subjects";
    }

    @PostMapping("/{id}/restore")
    public String restore (@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        subjectService.restore(id);
        redirectAttributes.addFlashAttribute("successMessage", "Курс відновлено з архіву");
        return "redirect:/subjects?archived=true";
    }

}
