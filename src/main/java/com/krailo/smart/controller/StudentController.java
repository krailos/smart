package com.krailo.smart.controller;

import com.krailo.smart.dto.OpeningBalanceDto;
import com.krailo.smart.dto.StudentDto;
import com.krailo.smart.enumeration.Gender;
import com.krailo.smart.enumeration.StudentStatus;
import com.krailo.smart.exception.BusinessException;
import com.krailo.smart.service.GangService;
import com.krailo.smart.service.PaymentReceivingService;
import com.krailo.smart.service.StudentService;
import com.krailo.smart.service.StudentsDiscountsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Set;

@Controller
@RequestMapping("/students")
@AllArgsConstructor
public class StudentController {

    private StudentService studentService;
    private GangService gangService;
    private StudentsDiscountsService studentsDiscountsService;
    private PaymentReceivingService paymentReceivingService;

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
        StudentDto student = studentService.findById(id);
        model.addAttribute("student", student);
        model.addAttribute("balance", student.getBalance());
        fillFormLists(model);
        return "student";
    }

    // групи учня (була /gangs/{id}/byStudent — там id був учня, а не групи)
    @GetMapping("/{id}/gangs")
    public String gangs(@PathVariable("id") Integer id, Model model) {
        studentService.findByIdEntity(id); // немає такого учня — 404
        // у шаблон — GangDto, як і в інших списках груп (gangs.html читає назву курсу, прізвище вчителя)
        model.addAttribute("gangs", gangService.findAllByStudent(id));
        return "gangs";
    }

    // знижки учня (була /studentsDiscounts/{id}/byStudent)
    @GetMapping("/{id}/discounts")
    public String discounts(@PathVariable("id") Integer id, Model model) {
        studentService.findByIdEntity(id); // немає такого учня — 404
        model.addAttribute("studentsDiscounts", studentsDiscountsService.findAllByStudentId(id));
        return "studentsDiscounts";
    }


    // Початковий баланс учня (облік вівся й до програми): + передоплата, - борг. Один раз на учня,
    // помилку виправляють сторнуванням у списку платежів (див. PaymentService.createOpeningBalance)
    @GetMapping("/{id}/opening-balance")
    public String openingBalanceForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("student", studentService.findById(id)); // немає такого учня — 404
        model.addAttribute("openingBalance", new OpeningBalanceDto(null, LocalDate.now(), "Початковий баланс"));
        return "studentOpeningBalance";
    }

    @PostMapping("/{id}/opening-balance")
    public String createOpeningBalance(@PathVariable("id") Integer id,
                                       @Valid @ModelAttribute("openingBalance") OpeningBalanceDto openingBalanceDto,
                                       BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("student", studentService.findById(id));
            return "studentOpeningBalance";
        }
        try {
            // оплата з позначкою + журнал транзакцій + баланс — одна транзакція
            paymentReceivingService.receiveOpeningBalance(id, openingBalanceDto);
        } catch (BusinessException e) {
            model.addAttribute("student", studentService.findById(id));
            model.addAttribute("errorMessage", e.getMessage());
            return "studentOpeningBalance";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Початковий баланс внесено");
        return "redirect:/students/" + id;
    }


    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("student", StudentDto.builder().build());
        fillFormLists(model);
        return "studentNew";
    }


    @PostMapping("/create")
    // @ResponseStatus(HttpStatus.CREATED)
    // @Valid + BindingResult: неправильні поля — та сама форма з текстом біля поля (див. AudienceController)
    public String create(@Valid @ModelAttribute("student") StudentDto student, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            fillFormLists(model);
            return "studentNew";
        }
        studentService.create(student);
        return "redirect:/students";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Integer id, @Valid @ModelAttribute("student") StudentDto studentDto,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // баланс у формі не надсилається — беремо з бази, щоб поле «Баланс» не було порожнім
            model.addAttribute("balance", studentService.findById(id).getBalance());
            fillFormLists(model);
            return "student";
        }
        studentService.update(id, studentDto);
        return "redirect:/students";
    }

    // варіанти для випадаючих меню «Стать» і «Статус»
    private void fillFormLists(Model model) {
        model.addAttribute("gender", Gender.values());
        model.addAttribute("status", StudentStatus.values());
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
