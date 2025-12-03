package com.fablab.backend.dto;

/**
 * Describes the implementation level of a control for a specific risk.
 *
 * @param riskId    identifier of the risk being evaluated
 * @param controlId identifier of the control whose maturity is tracked
 * @param level     measured implementation level (0, 0.5 or 1)
 */
public record ControlLevelDTO(
        Long riskId,
        Long controlId,
        double level
) {
}
