package com.krailo.smart.controller;

import com.krailo.smart.dto.*;
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
        SubjectDto subjectDto = scheduleDto.getSubjectDto();
        GangDto gangDto =scheduleDto.getGangDto();
        AudienceDto audienceDto = scheduleDto.getAudienceDto();
        LessonDto lessonDto = LessonDto.builder()
                .audienceDto(audienceDto)
                .subjectDto(subjectDto)
                .gangDto(gangDto)
                .startTime(scheduleDto.getStartTime())
                .endTime(scheduleDto.getEndTime()).build();

        // отримую студентів які повинні бути на уроці (через групу в якої урок)
        List<GangsStudentsDto> gangsStudentsDtoList = gangDto.getGangsStudentsDto();
        List<LessonsStudentsDto> lessonsStudentsDtoList = lessonDto.getLessonsStudentsDto();
        // передаю студентів з групи в урок
        for (GangsStudentsDto gangsStudentsDto : gangsStudentsDtoList) {
            lessonsStudentsDtoList.add(LessonsStudentsDto.builder()
                    .studentDto(studentService.findById(gangsStudentsDto.getStudentId()))
                    .build());
        }

        // передаю дані в модель
        model.addAttribute("lesson", lessonDto);
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
