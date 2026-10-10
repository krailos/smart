package com.krailo.smart.controller;

import com.krailo.smart.dto.GangsStudentsDto;
import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.mapper.LessonMapper;
import com.krailo.smart.repository.StudentRepository;
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
    private StudentService studentService;
    private LessonMapper lessonMapper;
    private LessonsStudentsService lessonsStudentsService;
    private BalanceStudentService balanceStudentService;
    private StudentRepository studentRepository;
    private AudienceService audienceService;
    private GangService gangService;
    private SubjectService subjectService;

    @GetMapping ("/lessons")
    public String findAll(Model model) {
        model.addAttribute("lessons", lessonService.findAll());
        return "lessons";
    }

    @PostMapping("/schedules/{id}/lesson")
    public String openLesson(@PathVariable("id") Integer scheduleId, Model model) {

        // отримую розклад
        ScheduleDto scheduleDto = scheduleService.findById(scheduleId);

        // заповнюю lessonDto даними, щоб передати у форму
        LessonDto lessonDto = LessonDto.builder().audienceId(scheduleDto.getAudienceId())
                .subjectId(scheduleDto.getSubjectId()).gangId(scheduleDto.getGangId())
                .startTime(scheduleDto.getStartTime()).endTime(scheduleDto.getEndTime()).build();

        // отримую студентів які повинні бути на уроці (через групу в якої урок), щоб передати у форму
        List<GangsStudentsDto> gangsStudentsDtoList = gangService.findById(scheduleDto.getGangId()).getGangsStudentsDto();
        List<StudentDto> studentsDtoList = new ArrayList<StudentDto>();
        for (GangsStudentsDto gangsStudentsDto : gangsStudentsDtoList) {
            studentsDtoList.add(studentService.findById(gangsStudentsDto.getStudentId()));
        }

        // передаю дані в модель
        model.addAttribute("subject", subjectService.findById(scheduleDto.getSubjectId()));
        model.addAttribute("gang", gangService.findById(scheduleDto.getGangId()));
        model.addAttribute("audience", audienceService.findById(scheduleDto.getAudienceId()));
        model.addAttribute("lesson", lessonDto);
        model.addAttribute("students", studentsDtoList);
        return "lessonNew";
    }

    @PostMapping("/lessons/create")
    public String closeLesson(@ModelAttribute LessonDto lessonDto) {
//        LessonDto lessonDtoWithId = lessonService.create(lessonDto);
//        Lesson lesson = lessonService.findByIdEntity(lessonDtoWithId.getId());
//        List<LessonsStudents> ls = lessonDto.getLessonsStudents();
//        for (LessonsStudents lessonsStudents : ls) {
//            lessonsStudents.setLesson(lesson);
//            lessonsStudentsService.create(lessonsStudents);
//        }
//        balanceStudentService.create(lesson);
        return "redirect:/lessons";
    }




}
