package com.fablab.backend.dto;

import com.fablab.backend.models.Asset;
import com.fablab.backend.models.AssetCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for creating or updating an {@link Asset}.
 *
 * @param analysisId  identifier of the analysis the asset belongs to
 * @param categoryId  identifier of the {@link AssetCategory}
 *                    assigned to the asset
 * @param name        asset name supplied by the user
 * @param description narrative description of the asset
 * @param impactC     confidentiality impact score ranging from 0 to 4
 * @param impactI     integrity impact score ranging from 0 to 4
 * @param impactA     availability impact score ranging from 0 to 4
 */
public record AssetRequest(@NotNull Long analysisId, @NotNull Short categoryId, @NotBlank String name,
                           @NotBlank String description, @Min(0) @Max(4) short impactC, @Min(0) @Max(4) short impactI,
                           @Min(0) @Max(4) short impactA) {
}
