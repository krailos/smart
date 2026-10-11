package com.krailo.smart.controller;

import com.krailo.smart.dto.AudienceDto;
import com.krailo.smart.dto.GangDto;
import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.dto.SubjectDto;
import com.krailo.smart.enumeration.WeekDay;
import com.krailo.smart.service.AudienceService;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.ScheduleService;
import com.krailo.smart.service.SubjectService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/schedules")
@AllArgsConstructor
public class ScheduleController {

    private ScheduleService scheduleService;
    private AudienceService audienceService;
    private SubjectService subjectService;
    private GangService gangService;

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
        ScheduleDto scheduleDto = ScheduleDto.builder().build();
        List<AudienceDto> audienceDtoList = audienceService.findAll();
        List<SubjectDto> subjectDtoList = subjectService.findAll();
        List<GangDto> gangDtoList = gangService.findAll();
        WeekDay [] weekDays = WeekDay.values();
        model.addAttribute("schedule",scheduleDto );
        model.addAttribute("audiences",audienceDtoList );
        model.addAttribute("subjects",subjectDtoList );
        model.addAttribute("gangs",gangDtoList );
        model.addAttribute("weekDays",weekDays );
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

