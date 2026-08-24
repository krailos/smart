package com.krailo.smart.controller;

import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.Subject;
import com.krailo.smart.service.ScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/schedules")
@AllArgsConstructor
public class ScheduleController {

    private ScheduleService scheduleService;


    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("schedules", scheduleService.findAll());
        return "schedules";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("schedule", scheduleService.findById(id));
        return "schedule";
    }

    @GetMapping("/schedule/new")
    public String createForm(Model model) {
        model.addAttribute("gang", new Subject());
        return "scheduleNew";
    }

    @PostMapping("/create")
    // @ResponseStatus(HttpStatus.CREATED)
    public String create(@ModelAttribute ScheduleDto scheduleDto) {
        scheduleService.create(scheduleDto);
        return "redirect:/schedules";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Integer id, @ModelAttribute ScheduleDto scheduleDto) {
        scheduleService.update(id, scheduleDto);
        return "redirect:/schedules";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Integer id) {
        scheduleService.delete(id);
        return "redirect:/schedules";
    }



}

