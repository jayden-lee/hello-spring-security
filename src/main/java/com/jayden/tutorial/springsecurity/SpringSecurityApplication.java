package com.jayden.tutorial.springsecurity;

import com.jayden.tutorial.springsecurity.domain.Account;
import com.jayden.tutorial.springsecurity.domain.AccountRepository;
import com.jayden.tutorial.springsecurity.domain.AccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAccountData(AccountService accountService) {
        return (args) -> {
            accountService.newAccount(Account.of("user", "123", "USER"));
            accountService.newAccount(Account.of("admin", "!@#", "ADMIN"));
        };
    }

}
