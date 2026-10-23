package com.krailo.smart.controller;

import com.krailo.smart.dto.AudienceDto;
import com.krailo.smart.dto.GangDto;
import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.dto.SubjectDto;
import com.krailo.smart.dto.TeacherDto;
import com.krailo.smart.enumeration.WeekDay;
import com.krailo.smart.exception.ScheduleConflictException;
import com.krailo.smart.service.AudienceService;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.ScheduleService;
import com.krailo.smart.service.SubjectService;
import com.krailo.smart.service.TeacherService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/schedules")
@AllArgsConstructor
public class
ScheduleController {

    private ScheduleService scheduleService;
    private AudienceService audienceService;
    private SubjectService subjectService;
    private GangService gangService;
        private TeacherService teacherService;

        @GetMapping
        public String findAll(Model model) {
            model.addAttribute("schedules", scheduleService.findAll());
            return "schedules";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("schedule", scheduleService.findById(id));
        fillFormLists(model);
        return "schedule";
    }

    @GetMapping("/schedule/new")
    public String createForm(Model model) {
        ScheduleDto scheduleDto = ScheduleDto.builder().build();
        model.addAttribute("schedule",scheduleDto );
        fillFormLists(model);
        return "scheduleNew";
    }

    @PostMapping("/create")
    // @ResponseStatus(HttpStatus.CREATED)
    public String create(@ModelAttribute ScheduleDto scheduleDto, Model model) {
        try {
            scheduleService.create(scheduleDto);
        } catch (ScheduleConflictException e) {
            // показуємо ту саму форму з уже введеними даними та текстом помилки
            model.addAttribute("schedule", scheduleDto);
            model.addAttribute("errorMessage", e.getMessage());
            fillFormLists(model);
            return "scheduleNew";
        }
        return "redirect:/schedules";
    }

    // списки для випадаючих меню форми розкладу
    private void fillFormLists(Model model) {
        List<AudienceDto> audienceDtoList = audienceService.findAll();
        List<SubjectDto> subjectDtoList = subjectService.findAll();
        List<GangDto> gangDtoList = gangService.findAll();
        List<TeacherDto> teacherDtoList = teacherService.findAll();
        WeekDay [] weekDays = WeekDay.values();
        model.addAttribute("audiences",audienceDtoList );
        model.addAttribute("subjects",subjectDtoList );
        model.addAttribute("gangs",gangDtoList );
        model.addAttribute("teachers",teacherDtoList );
        model.addAttribute("weekDays",weekDays );
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Integer id, @ModelAttribute ScheduleDto scheduleDto, Model model) {
        try {
            scheduleService.update(id, scheduleDto);
        } catch (ScheduleConflictException e) {
            scheduleDto.setId(id); // щоб форма знову відправлялась на /schedules/{id}/update
            model.addAttribute("schedule", scheduleDto);
            model.addAttribute("errorMessage", e.getMessage());
            fillFormLists(model);
            return "schedule";
        }
        return "redirect:/schedules";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Integer id) {
        scheduleService.delete(id);
        return "redirect:/schedules";
    }



}

