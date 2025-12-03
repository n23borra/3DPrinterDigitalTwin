package com.fablab.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisValidationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAnalysisInvalidPayloadReturnsBadRequest() throws Exception {
        var payload = new java.util.HashMap<String, Object>();
        payload.put("name", "");
        payload.put("description", "");
        payload.put("language", "");
        payload.put("scope", "");
        payload.put("criticality", null);
        payload.put("dm", 0);
        payload.put("ta", 0);

        mockMvc.perform(post("/api/analyses").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(payload))).andExpect(status().isBadRequest());
    }
}