package com.fablab.backend.dto;

import com.fablab.backend.models.RiskResult;
import com.fablab.backend.models.enums.TreatmentStrategy;

/**
 * Payload used to create an initial treatment plan for a risk result.
 *
 * @param riskResultId identifier of the {@link RiskResult}
 *                     being addressed
 * @param strategy     selected {@link TreatmentStrategy} for the plan
 */
public record TreatmentPlanCreateDTO(Long riskResultId, TreatmentStrategy strategy) {
}