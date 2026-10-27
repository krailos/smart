package com.krailo.smart.controller;

import com.krailo.smart.dto.GangDto;
import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.entity.Student;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/students")
@AllArgsConstructor
public class StudentController {

    private StudentService studentService;
    private GangService gangService;

    // за якими полями дозволено сортувати (ті, що є в заголовку таблиці)
    private static final Set<String> SORT_FIELDS = Set.of("id", "lastName", "balance");
    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping
    public String findAll(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(defaultValue = "lastName") String sortBy,
                          @RequestParam(defaultValue = "asc") String sortDir,
                          @RequestParam(required = false) String keyword,
                          // /students — активні, /students?archived=true — архів (той самий шаблон)
                          @RequestParam(defaultValue = "false") boolean archived,
                          Model model) {
        // параметри приходять з адреси (закладка, ручна правка, кнопка «‹» на першій сторінці дає page=-1) —
        // невалідне значення замінюємо допустимим, а не падаємо з 500
        page = Math.max(page, 0);
        if (size < 1 || size > MAX_PAGE_SIZE) {
            size = 10;
        }
        if (!SORT_FIELDS.contains(sortBy)) {
            sortBy = "lastName";
        }

        Page<StudentDto> studentPage  = studentService.findAllByPage(page, size, sortBy, sortDir, keyword, archived);
        model.addAttribute("archived", archived);
        model.addAttribute("studentPage", studentPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);
        return "students";
    }



    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("student", studentService.findById(id));
        model.addAttribute("gender", Gender.values());
        model.addAttribute("status", StudentStatus.values());
        return "student";
    }

    // групи учня (була /gangs/{id}/byStudent — там id був учня, а не групи)
    @GetMapping("/{id}/gangs")
    public String gangs(@PathVariable("id") Integer id, Model model) {
        Student student = studentService.findByIdEntity(id);
        // у шаблон — GangDto, як і в інших списках груп (gangs.html читає назву курсу, прізвище вчителя)
        List<GangDto> gangs = student.getGangsStudents().stream()
                .map(gs -> gangService.findById(gs.getGang().getId()))
                .toList();
        model.addAttribute("gangs", gangs);
        return "gangs";
    }

    // знижки учня (була /studentsDiscounts/{id}/byStudent)
    @GetMapping("/{id}/discounts")
    public String discounts(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("studentsDiscounts", studentService.findByIdEntity(id).getStudentsDiscounts());
        return "studentsDiscounts";
    }


    @GetMapping("/student/new")
    public String createForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("gender", Gender.values());
        model.addAttribute("status", StudentStatus.values());
        return "studentNew";
    }


    @PostMapping("/create")
    // @ResponseStatus(HttpStatus.CREATED)
    public String create(@ModelAttribute StudentDto student) {
        studentService.create(student);
        return "redirect:/students";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Integer id, @ModelAttribute StudentDto studentDto) {
        studentService.update(id, studentDto);
        return "redirect:/students";
    }

    // архівування замість видалення (див. StudentService.archive)
    @PostMapping("/{id}/archive")
    public String archive(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            studentService.archive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Учня переміщено в архів");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/students";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        studentService.restore(id);
        redirectAttributes.addFlashAttribute("successMessage", "Учня відновлено з архіву");
        return "redirect:/students?archived=true";
    }

}
