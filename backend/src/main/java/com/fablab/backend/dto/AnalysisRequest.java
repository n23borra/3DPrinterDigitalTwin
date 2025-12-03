package com.fablab.backend.dto;

import com.fablab.backend.models.enums.CriticalityLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload used to create or update an analysis definition together with
 * its context and preset parameters.
 *
 * @param name        human readable name shown to analysts
 * @param description narrative describing the assessment scope
 * @param language    preferred language code for generated artefacts
 * @param scope       textual scope statement captured from the user
 * @param criticality organisation criticality level driving threshold defaults
 * @param dm          defensive maturity score on a 1–5 scale
 * @param ta          threat actor capability score on a 1–5 scale
 */
public record AnalysisRequest(@NotBlank String name, @NotBlank String description, @NotBlank String language,
                              @NotBlank String scope, @NotNull CriticalityLevel criticality,
                              @Min(1) @Max(5) short dm,
                              @Min(1) @Max(5) short ta) {
}
