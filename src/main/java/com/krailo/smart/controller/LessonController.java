package com.krailo.smart.controller;

import com.krailo.smart.dto.GangsStudentsDto;
import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.repository.LessonsStudentsRepository;
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

    private final StudentService studentService;
    private final LessonsStudentsRepository lessonsStudentsRepository;
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
        List<LessonsStudentsDto> lessonsStudentsDto = lessonDto.getLessonsStudentsDto();
        // беру з бази урок щоб підтягнуло всю інфо (через кеш першого рівня)
        Lesson lesson = lessonService.findByIdEntity(lessonDtoWithId.getId());
        for (LessonsStudentsDto lsdto : lessonsStudentsDto) {
           LessonsStudents ls =  LessonsStudents.builder()
                    .lesson(lesson)
                    .student(studentService.findByIdEntity(lsdto.getStudentId()))
                    .present(lsdto.isPresent())
                    .payed(lsdto.isPayed())
                    .build();
            System.out.println("---- Test lesoonsStudent created-----");
            System.out.println(lessonsStudentsRepository.saveAndFlush(ls));
        }
        transactionStudentService.create(lessonDtoWithId);
        return "redirect:/lessons";
    }


}
