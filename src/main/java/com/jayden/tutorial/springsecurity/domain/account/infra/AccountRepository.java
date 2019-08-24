package com.jayden.tutorial.springsecurity.domain.account.infra;

import com.jayden.tutorial.springsecurity.domain.account.Account;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {

    private AccountJpaRepository jpaRepository;

    public AccountRepository(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public Account findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }

    public void save(Account account) {
        jpaRepository.save(account);
    }

}
