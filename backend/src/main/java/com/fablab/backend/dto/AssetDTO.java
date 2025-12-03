package com.fablab.backend.dto;

import com.fablab.backend.models.Asset;
import com.fablab.backend.models.Analysis;
import com.fablab.backend.models.AssetCategory;

/**
 * Projection of an {@link Asset} returned to the client with minimal
 * information about its owning analysis and category.
 *
 * @param id             technical identifier of the asset
 * @param analysisId     identifier of the parent analysis, when loaded
 * @param categoryId     identifier of the linked {@link AssetCategory}
 * @param categoryLabel  human readable label of the asset category
 * @param name           asset name captured during assessment
 * @param description    textual description clarifying the asset scope
 * @param impactC        confidentiality impact scored between 0 and 4
 * @param impactI        integrity impact scored between 0 and 4
 * @param impactA        availability impact scored between 0 and 4
 */
public record AssetDTO(
        Long id,
        Long analysisId,
        Short categoryId,
        String categoryLabel,
        String name,
        String description,
        Short impactC,
        Short impactI,
        Short impactA
) {
    public static AssetDTO from(Asset asset) {
        if (asset == null) return null;
        return new AssetDTO(
                asset.getId(),
                asset.getAnalysis() != null ? asset.getAnalysis().getId() : null,
                asset.getCategory() != null ? asset.getCategory().getId() : null,
                asset.getCategory() != null ? asset.getCategory().getLabel() : null,
                asset.getName(),
                asset.getDescription(),
                asset.getImpactC(),
                asset.getImpactI(),
                asset.getImpactA()
        );
    }

    public static Asset toEntity(AssetDTO dto) {
        if (dto == null) return null;
        Asset asset = new Asset();
        asset.setId(dto.id());
        if (dto.analysisId() != null) {
            Analysis analysis = new Analysis();
            analysis.setId(dto.analysisId());
            asset.setAnalysis(analysis);
        }
        if (dto.categoryId() != null) {
            AssetCategory category = new AssetCategory();
            category.setId(dto.categoryId());
            asset.setCategory(category);
        }
        asset.setName(dto.name());
        asset.setDescription(dto.description());
        asset.setImpactC(dto.impactC());
        asset.setImpactI(dto.impactI());
        asset.setImpactA(dto.impactA());
        return asset;
    }
}