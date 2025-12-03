package com.fablab.backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReferenceDataAccessTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userCanFetchThreats() throws Exception {
        mockMvc.perform(get("/api/threats").with(user("u").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void userCanFetchVulnerabilities() throws Exception {
        mockMvc.perform(get("/api/vulnerabilities").with(user("u").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void userCanFetchControls() throws Exception {
        mockMvc.perform(get("/api/controls").with(user("u").roles("USER")))
                .andExpect(status().isOk());
    }
}