package com.fablab.backend.controllers;

import com.fablab.backend.dto.RiskControlDTO;
import com.fablab.backend.dto.RiskRequest;
import com.cyber.backend.models.*;
import com.cyber.backend.repositories.*;
import com.fablab.backend.dto.ControlLevelDTO;
import com.fablab.backend.dto.RiskResultDTO;
import com.fablab.backend.models.*;
import com.fablab.backend.repositories.*;
import com.fablab.backend.services.RiskCalculationService;
import com.fablab.backend.services.TreatmentPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({RiskController.class, RiskCalculationService.class, TreatmentPlanService.class})
class RiskControllerControlsTest {

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
    @Autowired
    private ControlRepository controlRepository;

    @Test
    void listControlsReturnsAppliedControls() {
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

        // create risk via controller
        ResponseEntity<RiskResultDTO> resp = controller.create(new RiskRequest(asset.getId(), threat.getId(), vul.getId()));
        assertEquals(200, resp.getStatusCodeValue());
        Long riskId = resp.getBody().riskId();

        Control control = new Control();
        control.setLabel("C1");
        control.setEfficiency(0.5);
        controlRepository.save(control);

        controller.applyControl(new ControlLevelDTO(riskId, control.getId(), 1.0));

        ResponseEntity<List<RiskControlDTO>> listResp = controller.controls(riskId);
        assertEquals(200, listResp.getStatusCodeValue());
        List<RiskControlDTO> list = listResp.getBody();
        assertNotNull(list);
        assertEquals(1, list.size());
        RiskControlDTO dto = list.get(0);
        assertEquals(control.getId(), dto.controlId());
        assertEquals("C1", dto.label());
        assertEquals(1.0, dto.level());
    }

    @Test
    void applyingControlTwiceUpdatesExistingLevel() {
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

        // create risk via controller
        ResponseEntity<RiskResultDTO> resp = controller.create(new RiskRequest(asset.getId(), threat.getId(), vul.getId()));
        assertEquals(200, resp.getStatusCodeValue());
        Long riskId = resp.getBody().riskId();

        Control control = new Control();
        control.setLabel("C1");
        control.setEfficiency(0.5);
        controlRepository.save(control);

        controller.applyControl(new ControlLevelDTO(riskId, control.getId(), 0.5));
        controller.applyControl(new ControlLevelDTO(riskId, control.getId(), 1.0));

        ResponseEntity<List<RiskControlDTO>> listResp = controller.controls(riskId);
        assertEquals(200, listResp.getStatusCodeValue());
        List<RiskControlDTO> list = listResp.getBody();
        assertNotNull(list);
        assertEquals(1, list.size());
        RiskControlDTO dto = list.get(0);
        assertEquals(control.getId(), dto.controlId());
        assertEquals(1.0, dto.level());
    }
}