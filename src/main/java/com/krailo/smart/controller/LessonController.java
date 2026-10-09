package com.krailo.smart.controller;

import com.krailo.smart.dto.GangsStudentsDto;
import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.entity.GangsStudents;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.entity.Student;
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


        ScheduleDto scheduleDto = scheduleService.findById(scheduleId);
//        Lesson lesson = new Lesson();
//        lesson.setAudience(audienceService.findByIdEntity(scheduleDto.getAudienceId()));
//        lesson.setSubject(subjectService.findByIdEntity(scheduleDto.getSubjectId()));
//        lesson.setGang(gangService.findByIdEntity(scheduleDto.getGangId()));
//        lesson.setStartTime(scheduleDto.getStartTime());
//        lesson.setEndTime(scheduleDto.getEndTime());

        LessonDto lessonDto = LessonDto.builder().audienceId(scheduleDto.getAudienceId())
                .subjectId(scheduleDto.getSubjectId()).gangId(scheduleDto.getGangId())
                .startTime(scheduleDto.getStartTime()).endTime(scheduleDto.getEndTime()).build();


        // отримую студентів які повинні бути на уроці (через групу в якої урок)
        List<GangsStudentsDto> gangsStudentsDto = gangService.findById(scheduleDto.getGangId()).getGangsStudentsDto();
        List<StudentDto> studentsDto = new ArrayList<StudentDto>();
        for (GangsStudentsDto gsDto : gangsStudentsDto) {
            studentsDto.add(studentService.findById(gsDto.getStudentId()));
        }


        List<LessonsStudents> ls = new ArrayList<LessonsStudents>();
        for (Student student : students) {
            LessonsStudents lessonStudent = new LessonsStudents();
            lessonStudent.setStudent(student);
            ls.add(lessonStudent);
        }
        lesson.setLessonsStudents(ls);
        model.addAttribute("lesson", lesson);
        return "lessonNew";
    }

    @PostMapping("/lessons/create")
    public String closeLesson(@ModelAttribute LessonDto lessonDto) {
        //System.out.println(lessonDto.getLessonsStudents());
        LessonDto lessonDtoWithId = lessonService.create(lessonDto);
        //System.out.println(lessonDto);
        Lesson lesson = lessonService.findByIdEntity(lessonDtoWithId.getId());
        List<LessonsStudents> ls = lessonDto.getLessonsStudents();
        for (LessonsStudents lessonsStudents : ls) {
            lessonsStudents.setLesson(lesson);
            lessonsStudentsService.create(lessonsStudents);
        }
        balanceStudentService.create(lesson);
        return "redirect:/lessons";
    }




}
