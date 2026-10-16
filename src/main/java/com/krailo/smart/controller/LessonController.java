package com.krailo.smart.controller;

import com.krailo.smart.dto.GangsStudentsDto;
import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
public class LessonController {

    private LessonService lessonService;
    private ScheduleService scheduleService;
    private LessonsStudentsService lessonsStudentsService;
    private TransactionStudentService transactionStudentService;
    private GangService gangService;


    @GetMapping("/lessons")
    public String findAll(Model model) {
        model.addAttribute("lessons", lessonService.findAll());
        return "lessons";
    }

    @PostMapping("/schedules/{id}/lesson")
    public String openLesson(@PathVariable("id") Integer scheduleId, Model model) {
        // отримую розклад
        ScheduleDto scheduleDto = scheduleService.findById(scheduleId);
        // заповнюю lessonDto даними, щоб передати у форму
        LessonDto lessonDto = LessonDto.builder()
                .audienceId(scheduleDto.getAudienceId())
                .audienceName(scheduleDto.getAudienceName())
                .subjectId(scheduleDto.getSubjectId())
                .subjectName(scheduleDto.getSubjectName())
                .gangId(scheduleDto.getGangId())
                .gangName(scheduleDto.getGangName())
                .startTime(scheduleDto.getStartTime())
                .endTime(scheduleDto.getEndTime()).build();
        // отримую студентів які повинні бути на уроці (через групу в якої урок)
        List<GangsStudentsDto> gangsStudentsDtoList = gangService.findById(scheduleDto.getGangId()).getGangsStudentsDto();
        List<LessonsStudentsDto> lessonsStudentsDtoList = new ArrayList<LessonsStudentsDto>();
        // передаю студентів з групи в урок
        for (GangsStudentsDto gangsStudentsDto : gangsStudentsDtoList) {
            lessonsStudentsDtoList.add(LessonsStudentsDto.builder()
                    .studentId(gangsStudentsDto.getStudentId())
                    .studentLastName(gangsStudentsDto.getStudentLastName())
                    .studentFirstName(gangsStudentsDto.getStudentFirstName())
                    .build());
        }
        lessonDto.setLessonsStudentsDto(lessonsStudentsDtoList);

        // передаю дані в модель
        model.addAttribute("lesson", lessonDto);
        return "lessonNew";
    }

    @PostMapping("/lessons/create")
    public String closeLesson(@ModelAttribute LessonDto lessonDto) {
        LessonDto lessonDtoWithId = lessonService.create(lessonDto);
        List<LessonsStudentsDto> ls = lessonDto.getLessonsStudentsDto();
        for (LessonsStudentsDto lessonsStudentsDto : ls) {
            lessonsStudentsDto.setLessonId(lessonDtoWithId.getId());
            lessonsStudentsService.create(lessonsStudentsDto);
        }
        transactionStudentService.create(lessonDtoWithId);
        return "redirect:/lessons";
    }


}
