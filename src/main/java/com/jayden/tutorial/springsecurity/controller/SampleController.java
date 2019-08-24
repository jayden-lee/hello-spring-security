package com.jayden.tutorial.springsecurity.controller;

import com.jayden.tutorial.springsecurity.domain.account.AccountContext;
import com.jayden.tutorial.springsecurity.domain.account.infra.AccountRepository;
import com.jayden.tutorial.springsecurity.domain.sample.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class SampleController {

    @Autowired
    private SampleService sampleService;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping(value = "/")
    public String index(Model model, Principal principal) {
        if (principal == null) {
            model.addAttribute("message", "Hello Spring Security");
        } else {
            model.addAttribute("message", "Hello " + principal.getName());
        }
        return "index";
    }

    @GetMapping(value = "/info")
    public String info(Model model) {
        model.addAttribute("message", "Hello Info");
        return "info";
    }

    @GetMapping(value = "/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("message", "Hello " + principal.getName());
        AccountContext.setAccount(accountRepository.findByUsername(principal.getName()));
        sampleService.dashboard();
        return "dashboard";
    }

    @GetMapping(value = "/admin")
    public String admin(Model model, Principal principal) {
        model.addAttribute("message", "Hello Admin " + principal.getName());
        return "admin";
    }

}
