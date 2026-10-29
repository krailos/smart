package com.krailo.smart.controller;

import com.krailo.smart.dto.TeacherDto;
import com.krailo.smart.entity.Student;
import com.krailo.smart.report.Bill;
import com.krailo.smart.report.Report;
import com.krailo.smart.report.TeacherLessonsReport;
import com.krailo.smart.service.LessonService;
import com.krailo.smart.service.StudentService;
import com.krailo.smart.service.TeacherService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/reports")
@AllArgsConstructor
public class ReportController {

    private LessonService lessonService;
    private StudentService studentService;
    private Report report;
    private TeacherLessonsReport teacherLessonsReport;
    private TeacherService teacherService;

    // «Уроки вчителів»: форма і результат на одній сторінці. Без дат — поточний місяць, звіт ще не сформовано
    @GetMapping("/teacher-lessons")
    public String teacherLessons(@RequestParam(required = false) LocalDate dateStart,
                                 @RequestParam(required = false) LocalDate dateEnd,
                                 @RequestParam(required = false) Integer teacherId, Model model) {
        // у списку й архівні вчителі: за минулий період урок міг провести вчитель, якого вже немає в школі
        List<TeacherDto> teachers = new ArrayList<>(teacherService.findAll());
        teachers.addAll(teacherService.findAllArchived());
        teachers.sort(Comparator.comparing(TeacherDto::getLastName));
        model.addAttribute("teachers", teachers);
        model.addAttribute("teacherId", teacherId);
        if (dateStart == null || dateEnd == null) {
            LocalDate today = LocalDate.now();
            model.addAttribute("dateStart", today.withDayOfMonth(1));
            model.addAttribute("dateEnd", today.withDayOfMonth(today.lengthOfMonth()));
            return "reportTeacherLessons";
        }
        model.addAttribute("dateStart", dateStart);
        model.addAttribute("dateEnd", dateEnd);
        List<TeacherLessonsReport.TeacherLessons> result = teacherLessonsReport.create(dateStart, dateEnd, teacherId);
        model.addAttribute("result", result);
        model.addAttribute("totalCount", result.stream().mapToInt(TeacherLessonsReport.TeacherLessons::getCount).sum());
        model.addAttribute("totalMinutes", result.stream().mapToLong(TeacherLessonsReport.TeacherLessons::getMinutes).sum());
        return "reportTeacherLessons";
    }


    
    @GetMapping()
    public String findAll () {

        return "reports";
    }

    @GetMapping("/statement")
    public String showAccount(@RequestParam("dateStart") LocalDate dateStart,
                              @RequestParam("dateEnd") LocalDate dateEnd,  @RequestParam("studentId") String studentId, Model model) {
        Student student = studentService.findByIdEntity(Integer.valueOf(studentId));
        Bill bill = report.createBill(student, dateStart, dateEnd);
        model.addAttribute("bill", bill);
        return "reportAccount";

    }

    // параметри необов'язкові: кнопка «Інший період» з виписки передає учня й дати, щоб не вибирати учня знову
    @GetMapping("/statement/form")
    public String showAccountForm (@RequestParam(required = false) Integer studentId,
                                   @RequestParam(required = false) LocalDate dateStart,
                                   @RequestParam(required = false) LocalDate dateEnd, Model model) {
        // і активні, і архівні: учень в архіві теж може мати борг
        model.addAttribute("students", studentService.findAllIncludingArchived());
        model.addAttribute("studentId", studentId);
        // без дат (перший вхід з меню «Звіти») — поточний місяць
        LocalDate today = LocalDate.now();
        model.addAttribute("dateStart", dateStart != null ? dateStart : today.withDayOfMonth(1));
        model.addAttribute("dateEnd", dateEnd != null ? dateEnd : today.withDayOfMonth(today.lengthOfMonth()));
        return "reportAccountForm";
    }

}
