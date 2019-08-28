package com.jayden.tutorial.springsecurity;

import com.jayden.tutorial.springsecurity.domain.account.AccountDto;
import com.jayden.tutorial.springsecurity.domain.account.AccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAccountData(AccountService accountService) {
        return (args) -> {
            accountService.createNew(AccountDto.CreateRequest.of("user", "123", "USER"));
            accountService.createNew(AccountDto.CreateRequest.of("admin", "!@#", "ADMIN"));
        };
    }

}
