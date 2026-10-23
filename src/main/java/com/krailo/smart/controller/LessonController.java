package com.krailo.smart.controller;

import com.krailo.smart.dto.GangsStudentsDto;
import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.exception.ScheduleConflictException;
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
    private TeacherService teacherService;
    private AudienceService audienceService;
    private SubjectService subjectService;



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
                // за замовчуванням урок проводить вчитель з розкладу, у формі можна вибрати заміну
                .teacherId(scheduleDto.getTeacherId())
                .teacherName(scheduleDto.getTeacherName())
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
        model.addAttribute("teachers", teacherService.findAll());
        return "lessonNew";
    }

    @PostMapping("/lessons/create")
    public String closeLesson(@ModelAttribute LessonDto lessonDto, Model model) {
        LessonDto lessonDtoWithId;
        try {
            lessonDtoWithId = lessonService.create(lessonDto);
        } catch (ScheduleConflictException e) {
            // показуємо ту саму форму з відмітками учнів і текстом помилки
            fillNames(lessonDto);
            model.addAttribute("lesson", lessonDto);
            model.addAttribute("teachers", teacherService.findAll());
            model.addAttribute("errorMessage", e.getMessage());
            return "lessonNew";
        }
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
        transactionStudentService.createByLesson(lessonDtoWithId);
        return "redirect:/lessons";
    }

    @GetMapping("/lessons/{id}")
    public String findById(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("lesson", lessonService.findById(id));
        fillEditFormLists(model);
        return "lesson";
    }

    // редагуються тільки вчитель, аудиторія, дата і час;
    // відмітки учнів (присутність/оплата) тут не змінюються — від них залежить баланс
    @PostMapping("/lessons/{id}/update")
    public String update(@PathVariable("id") Integer id, @ModelAttribute LessonDto lessonDto, Model model) {
        try {
            lessonService.update(id, lessonDto);
        } catch (ScheduleConflictException e) {
            // беремо урок з бази (учні, назви) і накладаємо те, що користувач ввів у формі
            LessonDto lesson = lessonService.findById(id);
            lesson.setTeacherId(lessonDto.getTeacherId());
            lesson.setAudienceId(lessonDto.getAudienceId());
            lesson.setDate(lessonDto.getDate());
            lesson.setStartTime(lessonDto.getStartTime());
            lesson.setEndTime(lessonDto.getEndTime());
            model.addAttribute("lesson", lesson);
            model.addAttribute("errorMessage", e.getMessage());
            fillEditFormLists(model);
            return "lesson";
        }
        return "redirect:/lessons";
    }

    private void fillEditFormLists(Model model) {
        model.addAttribute("teachers", teacherService.findAll());
        model.addAttribute("audiences", audienceService.findAll());
    }

    // форма уроку надсилає тільки id (поля з назвами disabled і не відправляються),
    // тому для повторного показу форми підтягуємо назви з бази
    private void fillNames(LessonDto d) {
        d.setAudienceName(audienceService.findById(d.getAudienceId()).getName());
        d.setSubjectName(subjectService.findById(d.getSubjectId()).getName());
        d.setGangName(gangService.findById(d.getGangId()).getName());
        if (d.getLessonsStudentsDto() == null) {
            return;
        }
        for (LessonsStudentsDto ls : d.getLessonsStudentsDto()) {
            StudentDto student = studentService.findById(ls.getStudentId());
            ls.setStudentLastName(student.getLastName());
            ls.setStudentFirstName(student.getFirstName());
        }
    }


}
