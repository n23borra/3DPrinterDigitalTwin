package com.fablab.backend.services;

import com.fablab.backend.models.Analysis;
import com.fablab.backend.models.RiskBase;
import com.fablab.backend.models.RiskResult;
import com.fablab.backend.models.enums.RiskStatus;
import com.fablab.backend.repositories.ControlImplementationRepository;
import com.fablab.backend.repositories.RiskResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiskCalculationService {

    private final ControlImplementationRepository implRepo;
    private final RiskResultRepository resultRepo;

    /**
     * Recalculates the residual risk values for the provided risk base and persists the result.
     *
     * @param rb risk base entity to recalculate
     * @return the updated {@link RiskResult}
     */
    @Transactional
    public RiskResult recalculate(RiskBase rb) {

        double T = rb.getThreat().getProbability();
        double V = rb.getVulnerability().getGravity();
        double I = (rb.getAsset().getImpactC() + rb.getAsset().getImpactI() + rb.getAsset().getImpactA()) / 3.0;
        double r0 = T * V * I;

        double residual = implRepo.findByRisk_Id(rb.getId()).stream().mapToDouble(ci -> 1 - ci.getLevel() * ci.getControl().getEfficiency()).reduce(1.0, (a, b) -> a * b);

        double r1 = r0 * residual;
        double r2 = r1 * factor(rb.getAsset().getAnalysis().getDm());
        double r3 = r2 * factor(rb.getAsset().getAnalysis().getTa());
        double fr = r3;

        Analysis a = rb.getAsset().getAnalysis();
        RiskStatus status = fr < a.getS1() ? RiskStatus.ACCEPT : fr < a.getS2() ? RiskStatus.MONITOR : RiskStatus.TREAT;

        RiskResult rr = resultRepo.findByRisk_Id(rb.getId()).orElse(new RiskResult());
        rr.setRisk(rb);
        rr.setR0(r0);
        rr.setR1(r1);
        rr.setR2(r2);
        rr.setR3(r3);
        rr.setFr(fr);
        rr.setStatus(status);

        return resultRepo.save(rr);
    }

    private double factor(short level) {
        return switch (level) {
            case 1 -> 1.2;
            case 2 -> 1.1;
            case 3 -> 1.0;
            case 4 -> 0.9;
            case 5 -> 0.8;
            default -> 1.0;
        };
    }
}
