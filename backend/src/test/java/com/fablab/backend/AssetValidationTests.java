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
class AssetValidationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addAssetInvalidPayloadReturnsBadRequest() throws Exception {
        var payload = new java.util.HashMap<String, Object>();
        payload.put("analysisId", null);
        payload.put("categoryId", 1);
        payload.put("name", "");
        payload.put("description", "");
        payload.put("impactC", -1);
        payload.put("impactI", -1);
        payload.put("impactA", -1);

        mockMvc.perform(post("/api/assets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(payload))).andExpect(status().isBadRequest());
    }
}