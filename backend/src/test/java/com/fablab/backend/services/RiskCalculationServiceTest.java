package com.fablab.backend.services;

import com.cyber.backend.models.*;
import com.fablab.backend.models.*;
import com.fablab.backend.models.enums.RiskStatus;
import com.fablab.backend.repositories.ControlImplementationRepository;
import com.fablab.backend.repositories.RiskResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RiskCalculationServiceTest {

    private ControlImplementationRepository implRepo;
    private RiskResultRepository resultRepo;
    private RiskCalculationService service;

    @BeforeEach
    void setup() {
        implRepo = mock(ControlImplementationRepository.class);
        resultRepo = mock(RiskResultRepository.class);
        service = new RiskCalculationService(implRepo, resultRepo);
    }

    @ParameterizedTest
    @CsvSource({"1,5,1.2,0.8", "3,3,1.0,1.0", "5,1,0.8,1.2"})
    void usesDmAndTaFactors(short dm, short ta, double fDm, double fTa) {
        Analysis a = new Analysis();
        a.setS1(1);
        a.setS2(10);
        a.setDm(dm);
        a.setTa(ta);

        Asset asset = new Asset();
        asset.setAnalysis(a);
        asset.setImpactC((short) 3);
        asset.setImpactI((short) 3);
        asset.setImpactA((short) 3);

        Threat t = new Threat();
        t.setProbability(0.5);
        Vulnerability v = new Vulnerability();
        v.setGravity(0.5);

        RiskBase rb = new RiskBase();
        rb.setId(1L);
        rb.setAsset(asset);
        rb.setThreat(t);
        rb.setVulnerability(v);

        when(implRepo.findByRisk_Id(1L)).thenReturn(Collections.emptyList());
        when(resultRepo.findByRisk_Id(1L)).thenReturn(Optional.empty());
        when(resultRepo.save(any(RiskResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RiskResult rr = service.recalculate(rb);

        double r0 = 0.5 * 0.5 * 3; // base risk
        assertEquals(r0, rr.getR0(), 1e-6);
        assertEquals(r0, rr.getR1(), 1e-6); // no controls
        assertEquals(r0 * fDm, rr.getR2(), 1e-6);
        assertEquals(r0 * fDm * fTa, rr.getR3(), 1e-6);
        assertEquals(r0 * fDm * fTa, rr.getFr(), 1e-6);
    }

    @Test
    void appliesControlEfficiencies() {
        Analysis a = new Analysis();
        a.setS1(2);
        a.setS2(5);
        a.setDm((short) 1);
        a.setTa((short) 5);

        Asset asset = new Asset();
        asset.setAnalysis(a);
        asset.setImpactC((short) 3);
        asset.setImpactI((short) 3);
        asset.setImpactA((short) 3);

        Threat t = new Threat();
        t.setProbability(0.5);
        Vulnerability v = new Vulnerability();
        v.setGravity(0.5);

        RiskBase rb = new RiskBase();
        rb.setId(1L);
        rb.setAsset(asset);
        rb.setThreat(t);
        rb.setVulnerability(v);

        Control c1 = new Control();
        c1.setEfficiency(0.2);
        ControlImplementation ci1 = new ControlImplementation();
        ci1.setRisk(rb);
        ci1.setControl(c1);
        ci1.setLevel(0.5);

        Control c2 = new Control();
        c2.setEfficiency(0.5);
        ControlImplementation ci2 = new ControlImplementation();
        ci2.setRisk(rb);
        ci2.setControl(c2);
        ci2.setLevel(1.0);

        when(implRepo.findByRisk_Id(1L)).thenReturn(List.of(ci1, ci2));
        when(resultRepo.findByRisk_Id(1L)).thenReturn(Optional.empty());
        when(resultRepo.save(any(RiskResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RiskResult rr = service.recalculate(rb);

        double r0 = 0.5 * 0.5 * 3; // 0.75
        double residual = (1 - 0.5 * 0.2) * (1 - 1.0 * 0.5); // 0.45
        double expectedFr = r0 * residual * 1.2 * 0.8; // factors for dm=1 and ta=5

        assertEquals(expectedFr, rr.getFr(), 1e-6);
        assertEquals(RiskStatus.ACCEPT, rr.getStatus());

        ArgumentCaptor<RiskResult> captor = ArgumentCaptor.forClass(RiskResult.class);
        verify(resultRepo).save(captor.capture());
        assertEquals(expectedFr, captor.getValue().getFr(), 1e-6);
    }
}