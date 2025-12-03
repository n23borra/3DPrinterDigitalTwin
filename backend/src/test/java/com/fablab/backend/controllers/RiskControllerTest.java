package com.fablab.backend.controllers;

import com.fablab.backend.dto.RiskRequest;
import com.cyber.backend.models.*;
import com.cyber.backend.repositories.*;
import com.fablab.backend.models.*;
import com.fablab.backend.repositories.*;
import com.fablab.backend.services.RiskCalculationService;
import com.fablab.backend.services.TreatmentPlanService;
import com.fablab.backend.dto.RiskResultDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({RiskController.class, RiskCalculationService.class, TreatmentPlanService.class})
class RiskControllerTest {

    @Autowired
    private RiskController controller;

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
    void duplicateRiskReturnsConflict() {
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
        threat.setLabel("t");
        threat.setProbability(0.5);
        threatRepository.save(threat);

        Vulnerability vul = new Vulnerability();
        vul.setLabel("v");
        vul.setGravity(0.5);
        vulnerabilityRepository.save(vul);

        RiskRequest request = new RiskRequest(asset.getId(), threat.getId(), vul.getId());
        ResponseEntity<?> first = controller.create(request);
        assertEquals(200, first.getStatusCodeValue());

        ResponseEntity<?> second = controller.create(request);
        assertEquals(409, second.getStatusCodeValue());
    }


    @Test
    void listReturnsEnrichedRisk() {
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
        asset.setName("A1");
        asset.setImpactC((short)1);
        asset.setImpactI((short)1);
        asset.setImpactA((short)1);
        assetRepository.save(asset);

        Threat threat = new Threat();
        threat.setLabel("T1");
        threat.setProbability(0.5);
        threatRepository.save(threat);

        Vulnerability vul = new Vulnerability();
        vul.setLabel("V1");
        vul.setGravity(0.5);
        vulnerabilityRepository.save(vul);

        controller.create(new RiskRequest(asset.getId(), threat.getId(), vul.getId()));

        ResponseEntity<List<RiskResultDTO>> listResp = controller.list(analysis.getId());
        assertEquals(200, listResp.getStatusCodeValue());
        List<RiskResultDTO> body = listResp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        RiskResultDTO dto = body.get(0);
        assertNotNull(dto.risk());
        assertNotNull(dto.risk().asset());
        assertEquals("A1", dto.risk().asset().name());
        assertEquals("T1", dto.risk().threat().label());
        assertEquals("V1", dto.risk().vulnerability().label());
    }
}