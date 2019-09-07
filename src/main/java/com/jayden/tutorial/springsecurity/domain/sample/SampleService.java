package com.jayden.tutorial.springsecurity.domain.sample;

import com.jayden.tutorial.springsecurity.common.SecurityLogger;
import com.jayden.tutorial.springsecurity.domain.account.Account;
import com.jayden.tutorial.springsecurity.domain.account.AccountContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SampleService {

    @Secured("ROLE_USER")
    public void dashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        System.out.println("================");
        System.out.println(authentication);
        System.out.println(authentication.getName());
    }

    @Async
    public void asyncService() {
        SecurityLogger.log("Async Service");
        System.out.println("Async service is called");
    }

}
