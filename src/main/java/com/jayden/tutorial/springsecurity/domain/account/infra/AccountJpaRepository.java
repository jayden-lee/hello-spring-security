package com.jayden.tutorial.springsecurity.domain.account.infra;

import com.jayden.tutorial.springsecurity.domain.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

interface AccountJpaRepository extends JpaRepository<Account, Long> {

    Account findByUsername(String username);

}
