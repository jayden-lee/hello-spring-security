package com.jayden.tutorial.springsecurity.domain.account;

import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;

@Entity
@Getter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    private String role;

    private void setUsername(String username) {
        this.username = username;
    }

    private void setEncodePassword(String encodePassword) {
        this.password = encodePassword;
    }

    private void setRole(String role) {
        this.role = role;
    }

    public static Account of(String username, String password, String role, PasswordEncoder passwordEncoder) {
        Account account = new Account();
        account.setUsername(username);
        account.setEncodePassword(passwordEncoder.encode(password));
        account.setRole(role);
        return account;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("username", username)
                .append("role", role)
                .build();
    }

    public void changeRole(String role) {
        setRole(role);
    }

}
