package com.krailo.smart.controller;


import com.krailo.smart.dto.AccountDto;
import com.krailo.smart.entity.Account;
import com.krailo.smart.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/accounts")
@AllArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("accounts", accountService.findAll());
        return "accounts";
    }



    @GetMapping("/{id}")
    public String findById (@PathVariable("id") Integer id, Model model) {
        model.addAttribute("account", accountService.findById(id));
        return "account";
    }


    @GetMapping("/account/new")
    public String createForm (Model model) {
        model.addAttribute("account", new Account());
        return "audienceNew";
    }


    @PostMapping("/create")
    // @ResponseStatus(HttpStatus.CREATED)
    public String create ( @ModelAttribute AccountDto accountDto) {
        accountService.create(accountDto);
        return "redirect:/accounts";
    }

    @PostMapping("/{id}/update")
    public String update (@PathVariable("id") Integer id, @ModelAttribute AccountDto accountDto) {
        accountService.update(id, accountDto);
        return "redirect:/accounts";
    }

    @PostMapping("/{id}/delete")
    public String delete (@PathVariable("id") Integer id) {
        accountService.delete(id);
        return "redirect:/accounts";
    }

}
