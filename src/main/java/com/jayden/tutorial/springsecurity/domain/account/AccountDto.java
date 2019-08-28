package com.jayden.tutorial.springsecurity.domain.account;

import lombok.Getter;
import lombok.Setter;

public class AccountDto {

    @Getter
    @Setter
    public static class CreateRequest {

        private String username;

        private String password;

        private String role = "USER";

        public static CreateRequest of(String username, String password, String role) {
            CreateRequest request = new CreateRequest();
            request.username = username;
            request.password = password;
            request.role = role;
            return request;
        }
    }
}
