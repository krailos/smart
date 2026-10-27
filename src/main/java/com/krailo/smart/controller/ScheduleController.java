package com.krailo.smart.controller;

import com.krailo.smart.calendar.ScheduleCalendar;
import com.krailo.smart.config.CurrentUser;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private ScheduleCalendar scheduleCalendar;
    private CurrentUser currentUser;

        @GetMapping
        public String findAll(Model model) {
            // адмін — увесь розклад, вчитель — тільки свої заняття
            model.addAttribute("schedules", currentUser.isAdmin()
                    ? scheduleService.findAll()
                    : scheduleService.findAllByTeacher(currentUser.teacherId()));
            return "schedules";
    }

    // Тижневий календар — додатковий вигляд розкладу. Фільтр (аудиторія / вчитель / група) робить блідими
    // заняття інших — так видно, коли саме вибране вільне. Параметри навмисно не audienceId/…, щоб не плутати з формою.
    @GetMapping("/week")
    public String week(@RequestParam(required = false) Integer audience,
                       @RequestParam(required = false) Integer teacher,
                       @RequestParam(required = false) Integer gang, Model model) {
        model.addAttribute("calendar", scheduleCalendar.build(scheduleService.findAll(), audience, teacher, gang));
        model.addAttribute("filterAudience", audience);
        model.addAttribute("filterTeacher", teacher);
        model.addAttribute("filterGang", gang);
        // форма у вікні: після конфлікту — те, що вводили (flash "draft"), інакше — порожня з підставленим фільтром
        if (!model.containsAttribute("draft")) {
            model.addAttribute("draft", ScheduleDto.builder().audienceId(audience).teacherId(teacher).gangId(gang).build());
        }
        fillFormLists(model);
        return "scheduleWeek";
    }

    // додавання з календаря: ті самі перевірки конфліктів, але повертаємось на календар (з тим самим фільтром)
    @PostMapping("/week/create")
    public String createFromWeek(@ModelAttribute ScheduleDto scheduleDto,
                                 @RequestParam(required = false) Integer filterAudience,
                                 @RequestParam(required = false) Integer filterTeacher,
                                 @RequestParam(required = false) Integer filterGang,
                                 RedirectAttributes redirectAttributes) {
        try {
            scheduleService.create(scheduleDto);
            redirectAttributes.addFlashAttribute("successMessage", "Заняття додано в розклад");
        } catch (ScheduleConflictException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("draft", scheduleDto);
            redirectAttributes.addFlashAttribute("openForm", true);
        }
        return backToWeek(redirectAttributes, filterAudience, filterTeacher, filterGang);
    }

    // редагування з календаря (клік на заняття): ті самі перевірки, повертаємось на календар;
    // після конфлікту вікно відкривається знову в режимі редагування (draft.id != 0)
    @PostMapping("/week/{id}/update")
    public String updateFromWeek(@PathVariable("id") Integer id, @ModelAttribute ScheduleDto scheduleDto,
                                 @RequestParam(required = false) Integer filterAudience,
                                 @RequestParam(required = false) Integer filterTeacher,
                                 @RequestParam(required = false) Integer filterGang,
                                 RedirectAttributes redirectAttributes) {
        try {
            scheduleService.update(id, scheduleDto);
            redirectAttributes.addFlashAttribute("successMessage", "Заняття змінено");
        } catch (ScheduleConflictException e) {
            scheduleDto.setId(id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("draft", scheduleDto);
            redirectAttributes.addFlashAttribute("openForm", true);
        }
        return backToWeek(redirectAttributes, filterAudience, filterTeacher, filterGang);
    }

    // назад на календар з тим самим фільтром
    private String backToWeek(RedirectAttributes redirectAttributes, Integer audience, Integer teacher, Integer gang) {
        if (audience != null) redirectAttributes.addAttribute("audience", audience);
        if (teacher != null) redirectAttributes.addAttribute("teacher", teacher);
        if (gang != null) redirectAttributes.addAttribute("gang", gang);
        return "redirect:/schedules/week";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("schedule", scheduleService.findById(id));
        fillFormLists(model);
        return "schedule";
    }

    @GetMapping("/new")
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

