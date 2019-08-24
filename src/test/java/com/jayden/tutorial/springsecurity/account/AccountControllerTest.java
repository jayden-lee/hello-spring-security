package com.jayden.tutorial.springsecurity.account;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    static final String INDEX_PAGE = "/";
    static final String ADMIN_PAGE = "/admin";

    @Test
    @WithAnonymousUser
    public void index_anonymous() throws Exception {
        mockMvc.perform(get(INDEX_PAGE))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithNormalUser
    public void index_user() throws Exception {
        mockMvc.perform(get(INDEX_PAGE))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithNormalUser
    public void admin_user() throws Exception {
        mockMvc.perform(get(ADMIN_PAGE))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @WithAdminUser
    public void admin_admin() throws Exception {
        mockMvc.perform(get(ADMIN_PAGE))
            .andDo(print())
            .andExpect(status().isOk());
    }

}
