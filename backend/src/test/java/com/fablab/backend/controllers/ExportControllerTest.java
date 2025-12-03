package com.fablab.backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exportJsonReturnsJson() throws Exception {
        mockMvc.perform(get("/api/export/json")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void exportCsvReturnsCsv() throws Exception {
        mockMvc.perform(get("/api/export/csv")).andExpect(status().isOk()).andExpect(content().contentType("text/csv"));
    }
}