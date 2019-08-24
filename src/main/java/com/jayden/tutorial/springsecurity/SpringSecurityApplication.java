package com.jayden.tutorial.springsecurity;

import com.jayden.tutorial.springsecurity.domain.account.Account;
import com.jayden.tutorial.springsecurity.domain.account.AccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SpringSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAccountData(AccountService accountService, PasswordEncoder passwordEncoder) {
        return (args) -> {
            accountService.newAccount(Account.of("user", "123", "USER", passwordEncoder));
            accountService.newAccount(Account.of("admin", "!@#", "ADMIN", passwordEncoder));
        };
    }

}
