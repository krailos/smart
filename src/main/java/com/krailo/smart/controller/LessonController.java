package com.krailo.smart.controller;

import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.entity.GangsStudents;
import com.krailo.smart.entity.Lesson;
import com.krailo.smart.entity.LessonsStudents;
import com.krailo.smart.entity.Student;
import com.krailo.smart.mapper.LessonMapper;
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
    private GangService gangService;
    private LessonMapper lessonMapper;
    private LessonsStudentsService lessonsStudentsService;

    @GetMapping ("/lessons")
    public String findAll(Model model) {
        model.addAttribute("lessons", lessonService.findAll());
        return "lessons";
    }

    @PostMapping("/schedules/{id}/lesson")
    public String openLesson(@PathVariable("id") Integer id, Model model) {
        Lesson lesson = new Lesson();

        ScheduleDto scheduleDto = scheduleService.findById(id);
        lesson.setAudience(scheduleDto.getAudience());
        lesson.setSubject(scheduleDto.getSubject());
        lesson.setGang(scheduleDto.getGang());
        lesson.setStartTime(scheduleDto.getStartTime());
        lesson.setEndTime(scheduleDto.getEndTime());

        List<Student> students = new ArrayList<Student>();
        for (GangsStudents gs : scheduleDto.getGang().getGangStudents()) {
            students.add(gs.getStudent());
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
        return "redirect:/lessons";
    }




}
