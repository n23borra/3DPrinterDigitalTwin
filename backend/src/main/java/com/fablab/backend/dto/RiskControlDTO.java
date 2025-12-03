package com.fablab.backend.dto;

import com.fablab.backend.models.ControlImplementation;

/**
 * Projection summarising how a control mitigates a risk.
 *
 * @param controlId identifier of the referenced control
 * @param label     display label of the control
 * @param level     implementation level applied to the risk (0, 0.5 or 1)
 */
public record RiskControlDTO(
        Long controlId,
        String label,
        double level
) {
    public static RiskControlDTO fromEntity(ControlImplementation ci) {
        if (ci == null) return null;
        return new RiskControlDTO(
                ci.getControl() != null ? ci.getControl().getId() : null,
                ci.getControl() != null ? ci.getControl().getLabel() : null,
                ci.getLevel()
        );
    }
}