package com.jayden.tutorial.springsecurity.domain.sample;

import com.jayden.tutorial.springsecurity.domain.account.Account;
import com.jayden.tutorial.springsecurity.domain.account.AccountContext;
import org.springframework.stereotype.Service;

@Service
public class SampleService {

    public void dashboard() {
        // SecurityContextHolder에서 SecurityContext를 꺼낸 것처럼 사용
        Account account = AccountContext.getAccount();
        System.out.println("=========================");
        System.out.println(account);
    }

}
