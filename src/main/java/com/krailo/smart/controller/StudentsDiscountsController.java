package com.krailo.smart.controller;

import com.krailo.smart.dto.StudentsDiscountsDto;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.DiscountService;
import com.krailo.smart.service.StudentService;
import com.krailo.smart.service.StudentsDiscountsService;
import com.krailo.smart.service.SubjectService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/student-discounts")
@AllArgsConstructor
public class StudentsDiscountsController {

    private StudentsDiscountsService studentsDiscountsService;
    private StudentService studentService;
    private DiscountService discountService;
    private SubjectService subjectService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("studentsDiscounts", studentsDiscountsService.findAll());
        return "studentsDiscounts";
    }



    // знижки одного учня — у StudentController: /students/{id}/discounts

    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
        model.addAttribute("studentDiscount", studentsDiscountsService.findById(id));
        fillFormLists(model);
        return "studentDiscount";
    }


    @GetMapping("/new")
    public String createForm (Model model) {
        // порожня форма; дата за замовчуванням — сьогодні
        model.addAttribute("studentDiscount", StudentsDiscountsDto.builder().date(LocalDate.now()).build());
        fillFormLists(model);
        return "studentsDiscountsNew";
    }


    @PostMapping("/create")
   // @ResponseStatus(HttpStatus.CREATED)
    // @Valid + BindingResult: неправильні поля — та сама форма з текстом біля поля (див. AudienceController)
    public String create (@Valid @ModelAttribute("studentDiscount") StudentsDiscountsDto studentsDiscountsDto,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            fillFormLists(model);
            return "studentsDiscountsNew";
        }
        try {
            studentsDiscountsService.create(studentsDiscountsDto);
        } catch (BusinessException e) {
            // така сама знижка учня вже є — текст біля поля «Знижка»
            bindingResult.rejectValue("discountId", "duplicate", e.getMessage());
            fillFormLists(model);
            return "studentsDiscountsNew";
        }
        return "redirect:/student-discounts";
    }

    @PostMapping("/{id}/update")
    public String update (@PathVariable("id") Integer id,
                          @Valid @ModelAttribute("studentDiscount") StudentsDiscountsDto studentsDiscountsDto,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            fillFormLists(model);
            return "studentDiscount";
        }
        studentsDiscountsService.update(id, studentsDiscountsDto);
        return "redirect:/student-discounts";
    }

    // варіанти для випадаючих меню «Учень», «Знижка», «Курс»
    private void fillFormLists(Model model) {
        model.addAttribute("students", studentService.findAll());
        model.addAttribute("discounts", discountService.findAll());
        model.addAttribute("subjects", subjectService.findAll());
    }
    
    @PostMapping("/{id}/delete")
    public String delete (@PathVariable("id") Integer id) {
         studentsDiscountsService.delete(id); 
        return "redirect:/student-discounts";        
    }

}
