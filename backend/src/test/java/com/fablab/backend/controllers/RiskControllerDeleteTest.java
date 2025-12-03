package com.fablab.backend.controllers;

import com.cyber.backend.models.*;
import com.fablab.backend.models.*;
import com.fablab.backend.models.enums.RiskStatus;
import com.cyber.backend.repositories.*;
import com.fablab.backend.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RiskControllerDeleteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RiskBaseRepository riskBaseRepository;
    @Autowired
    private RiskResultRepository riskResultRepository;
    @Autowired
    private AnalysisRepository analysisRepository;
    @Autowired
    private AssetCategoryRepository assetCategoryRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private ThreatRepository threatRepository;
    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;

    @Test
    void deleteRiskRemovesCascade() throws Exception {
        Analysis analysis = new Analysis();
        analysis.setS1(1);
        analysis.setS2(10);
        analysis.setDm((short)3);
        analysis.setTa((short)3);
        analysisRepository.save(analysis);

        AssetCategory cat = new AssetCategory();
        cat.setLabel("cat");
        assetCategoryRepository.save(cat);

        Asset asset = new Asset();
        asset.setAnalysis(analysis);
        asset.setCategory(cat);
        asset.setImpactC((short)1);
        asset.setImpactI((short)1);
        asset.setImpactA((short)1);
        assetRepository.save(asset);

        Threat threat = new Threat();
        threat.setProbability(0.1);
        threatRepository.save(threat);

        Vulnerability vul = new Vulnerability();
        vul.setGravity(0.1);
        vulnerabilityRepository.save(vul);

        RiskBase rb = new RiskBase();
        rb.setAsset(asset);
        rb.setThreat(threat);
        rb.setVulnerability(vul);
        riskBaseRepository.save(rb);

        RiskResult rr = new RiskResult();
        rr.setRisk(rb);
        rr.setStatus(RiskStatus.ACCEPT);
        riskResultRepository.save(rr);

        mockMvc.perform(delete("/api/risks/" + rb.getId()))
                .andExpect(status().isOk());

        assertFalse(riskBaseRepository.findById(rb.getId()).isPresent());
        assertTrue(riskResultRepository.findAll().isEmpty());
    }
}