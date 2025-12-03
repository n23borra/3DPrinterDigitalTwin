package com.fablab.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void updateNonexistentAnalysisReturnsNotFound() throws Exception {
        var payload = new HashMap<String, Object>();
        payload.put("name", "n");
        payload.put("description", "d");
        payload.put("language", "en");
        payload.put("scope", "s");
        payload.put("criticality", "LOW");
        payload.put("dm", 1);
        payload.put("ta", 1);

        mockMvc.perform(put("/api/analyses/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNonexistentAnalysisReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/analyses/99999"))
                .andExpect(status().isNotFound());
    }
}