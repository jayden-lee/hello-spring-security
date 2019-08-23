package com.jayden.tutorial.springsecurity.domain;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Account {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    private String role;

    private void setUsername(String username) {
        this.username = username;
    }

    private void setEncodePassword(String password) {
        this.password = "{noop}" + password;
    }

    private void setRole(String role) {
        this.role = role;
    }

    public static Account of(String username, String password, String role) {
        Account account = new Account();
        account.setUsername(username);
        account.setEncodePassword(password);
        account.setRole(role);
        return account;
    }

}
