package com.fablab.backend.dto;

import com.fablab.backend.models.RiskBase;
import com.fablab.backend.models.Asset;
import com.fablab.backend.models.Threat;
import com.fablab.backend.models.Vulnerability;

/**
 * Aggregated view of a {@link RiskBase} combining identifiers and nested
 * descriptors for display.
 *
 * @param id              identifier of the risk record
 * @param assetId         identifier of the impacted {@link Asset}
 * @param threatId        identifier of the relevant {@link Threat}
 * @param vulnerabilityId identifier of the exploited {@link Vulnerability}
 * @param asset           embedded asset projection for UI convenience
 * @param threat          embedded threat projection for UI convenience
 * @param vulnerability   embedded vulnerability projection for UI convenience
 */
public record RiskBaseDTO(
        Long id,
        Long assetId,
        Long threatId,
        Long vulnerabilityId,
        AssetDTO asset,
        ThreatDTO threat,
        VulnerabilityDTO vulnerability
) {
    public static RiskBaseDTO fromEntity(RiskBase r) {
        if (r == null) return null;
        return new RiskBaseDTO(
                r.getId(),
                r.getAsset() != null ? r.getAsset().getId() : null,
                r.getThreat() != null ? r.getThreat().getId() : null,
                r.getVulnerability() != null ? r.getVulnerability().getId() : null,
                AssetDTO.from(r.getAsset()),
                ThreatDTO.from(r.getThreat()),
                VulnerabilityDTO.from(r.getVulnerability())
        );
    }
}
