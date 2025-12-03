package com.fablab.backend.services;

import com.cyber.backend.models.*;
import com.cyber.backend.repositories.*;
import com.fablab.backend.models.*;
import com.fablab.backend.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(RiskCalculationService.class)
class RiskCalculationServicePersistenceTest {

    @Autowired
    private RiskCalculationService service;

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
    @Autowired
    private RiskBaseRepository riskBaseRepository;
    @Autowired
    private RiskResultRepository riskResultRepository;

    @Test
    void recalculationPersistsRiskResult() {
        Analysis analysis = new Analysis();
        analysis.setS1(1);
        analysis.setS2(10);
        analysis.setDm((short)3);
        analysis.setTa((short)3);
        analysisRepository.save(analysis);

        AssetCategory cat = new AssetCategory();
        cat.setLabel("test");
        assetCategoryRepository.save(cat);

        Asset asset = new Asset();
        asset.setAnalysis(analysis);
        asset.setCategory(cat);
        asset.setImpactC((short)3);
        asset.setImpactI((short)3);
        asset.setImpactA((short)3);
        assetRepository.save(asset);

        Threat threat = new Threat();
        threat.setProbability(0.5);
        threatRepository.save(threat);

        Vulnerability vul = new Vulnerability();
        vul.setGravity(0.5);
        vulnerabilityRepository.save(vul);

        RiskBase rb = new RiskBase();
        rb.setAsset(asset);
        rb.setThreat(threat);
        rb.setVulnerability(vul);
        riskBaseRepository.save(rb);

        RiskResult result = service.recalculate(rb);

        Optional<RiskResult> stored = riskResultRepository.findById(result.getId());
        assertTrue(stored.isPresent());
        assertEquals(result.getFr(), stored.get().getFr());
    }
}