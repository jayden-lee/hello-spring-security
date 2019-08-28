package com.jayden.tutorial.springsecurity.controller;

import com.jayden.tutorial.springsecurity.domain.account.AccountDto;
import com.jayden.tutorial.springsecurity.domain.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/signup")
public class SignUpController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public String signupForm(Model model) {
        model.addAttribute("account", new AccountDto.CreateRequest());
        return "signup";
    }

    @PostMapping
    public String processSignUp(@ModelAttribute AccountDto.CreateRequest request) {
        accountService.createNew(request);
        return "redirect:/";
    }

}
