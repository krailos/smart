package com.krailo.smart.controller;

import com.krailo.smart.config.CurrentUser;
import com.krailo.smart.dto.AudienceDto;
import com.krailo.smart.dto.GangsStudentsDto;
import com.krailo.smart.dto.TeacherDto;
import com.krailo.smart.dto.LessonDto;
import com.krailo.smart.dto.LessonsStudentsDto;
import com.krailo.smart.dto.ScheduleDto;
import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
public class LessonController {

    private final StudentService studentService;
    private LessonService lessonService;
    private LessonClosingService lessonClosingService;
    private ScheduleService scheduleService;
    private LessonsStudentsService lessonsStudentsService;
    private GangService gangService;
    private TeacherService teacherService;
    private AudienceService audienceService;
    private SubjectService subjectService;
    private CurrentUser currentUser;



    @GetMapping("/lessons")
    public String findAll(Model model) {
        // адмін — усі уроки, вчитель — свої (провів сам або з його заняття)
        model.addAttribute("lessons", currentUser.isAdmin()
                ? lessonService.findAll()
                : lessonService.findAllOfTeacher(currentUser.teacherId()));
        return "lessons";
    }

    // форма проведення уроку з цього заняття розкладу: лише показує форму, нічого не змінює — тому GET
    // (зберігає урок уже POST /lessons/create)
    @GetMapping("/schedules/{id}/lesson/new")
    public String openLesson(@PathVariable("id") Integer scheduleId, Model model) {
        // отримую розклад
        ScheduleDto scheduleDto = scheduleService.findById(scheduleId);
        // вчитель проводить урок тільки зі свого заняття (чуже -> 403)
        currentUser.requireOwnSchedule(scheduleDto.getTeacherId());
        // заповнюю lessonDto даними, щоб передати у форму
        LessonDto lessonDto = LessonDto.builder()
                .scheduleId(scheduleId) // урок походить від цього слоту розкладу
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
        // ще раз: форму могли підробити, підставивши чуже заняття
        if (lessonDto.getScheduleId() != null) {
            currentUser.requireOwnSchedule(scheduleService.findById(lessonDto.getScheduleId()).getTeacherId());
        }
        try {
            // урок + відмітки учнів + списання — одна транзакція (див. LessonClosingService)
            lessonClosingService.closeLesson(lessonDto);
        } catch (BusinessException e) {
            // нічого не збережено — показуємо ту саму форму з відмітками учнів і текстом помилки
            fillNames(lessonDto);
            model.addAttribute("lesson", lessonDto);
            model.addAttribute("teachers", teacherService.findAll());
            model.addAttribute("errorMessage", e.getMessage());
            return "lessonNew";
        }
        return "redirect:/lessons";
    }

    @GetMapping("/lessons/{id}")
    public String findById(@PathVariable("id") Integer id, Model model) {
        LessonDto lesson = lessonService.findById(id);
        model.addAttribute("lesson", lesson);
        fillEditFormLists(model, lesson.getTeacherId(), lesson.getAudienceId());
        return "lesson";
    }

    // редагуються вчитель (хто провів урок), аудиторія і дата; час — тільки перегляд (його задає розклад);
    // відмітки учнів (присутність/оплата) тут не змінюються — від них залежить баланс
    @PostMapping("/lessons/{id}/update")
    public String update(@PathVariable("id") Integer id, @ModelAttribute LessonDto lessonDto, Model model) {
        try {
            lessonService.update(id, lessonDto);
        } catch (BusinessException e) {
            // беремо урок з бази (учні, назви, час) і накладаємо те, що користувач вибрав у формі
            LessonDto lesson = lessonService.findById(id);
            fillEditFormLists(model, lesson.getTeacherId(), lesson.getAudienceId());
            lesson.setTeacherId(lessonDto.getTeacherId());
            lesson.setAudienceId(lessonDto.getAudienceId());
            lesson.setDate(lessonDto.getDate());
            model.addAttribute("lesson", lesson);
            model.addAttribute("errorMessage", e.getMessage());
            return "lesson";
        }
        return "redirect:/lessons";
    }

    // Сторнування уроку: гроші повертаються учням, урок лишається в історії з позначкою,
    // а слот розкладу на цю дату звільняється — урок можна провести заново
    @PostMapping("/lessons/{id}/storno")
    public String storno(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            lessonClosingService.stornoLesson(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Урок сторновано, гроші повернуто учням. За потреби проведіть урок заново з розкладу");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/lessons";
    }

    // Списки для форми редагування уроку: активні вчителі й аудиторії + ті, що зараз записані в уроці.
    // Вчитель старого уроку міг потрапити в архів — якщо його не буде в списку,
    // випадаюче меню при збереженні "тихо" замінить його першим вчителем зі списку.
    private void fillEditFormLists(Model model, Integer lessonTeacherId, Integer lessonAudienceId) {
        List<TeacherDto> teachers = new ArrayList<>(teacherService.findAll());
        if (teachers.stream().noneMatch(t -> t.getId().equals(lessonTeacherId))) {
            teachers.add(teacherService.findById(lessonTeacherId));
        }
        List<AudienceDto> audiences = new ArrayList<>(audienceService.findAll());
        if (audiences.stream().noneMatch(a -> a.getId().equals(lessonAudienceId))) {
            audiences.add(audienceService.findById(lessonAudienceId));
        }
        model.addAttribute("teachers", teachers);
        model.addAttribute("audiences", audiences);
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
