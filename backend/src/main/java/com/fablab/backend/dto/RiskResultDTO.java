package com.fablab.backend.dto;

import com.fablab.backend.models.RiskBase;
import com.fablab.backend.models.RiskResult;
import com.fablab.backend.models.enums.RiskStatus;

/**
 * Representation of calculated risk results returned to the frontend.
 *
 * @param id      identifier of the persisted risk result
 * @param fr      final residual risk score
 * @param r0      inherent risk value before treatment
 * @param r1      residual risk after planned treatments
 * @param r2      residual risk with accepted deviations
 * @param r3      residual risk considering proposed controls
 * @param status  workflow status for the result
 * @param riskId  identifier of the associated {@link RiskBase}
 * @param risk    embedded risk details used for display
 */
public record RiskResultDTO(
        Long id,
        Double fr,
        Double r0,
        Double r1,
        Double r2,
        Double r3,
        RiskStatus status,
        Long riskId,
        RiskBaseDTO risk
) {
    public static RiskResultDTO fromEntity(RiskResult r) {
        return fromEntity(r, r != null ? r.getRisk() : null);
    }

    public static RiskResultDTO fromEntity(RiskResult r, RiskBase base) {
        if (r == null) return null;
        RiskBase rb = base != null ? base : r.getRisk();
        return new RiskResultDTO(
                r.getId(),
                r.getFr(),
                r.getR0(),
                r.getR1(),
                r.getR2(),
                r.getR3(),
                r.getStatus(),
                rb != null ? rb.getId() : null,
                RiskBaseDTO.fromEntity(rb)
        );
    }
}
