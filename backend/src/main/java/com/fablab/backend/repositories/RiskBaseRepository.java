package com.fablab.backend.repositories;

import com.fablab.backend.models.RiskBase;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository managing {@link RiskBase} entities.
 */
public interface RiskBaseRepository extends JpaRepository<RiskBase, Long> {

    /**
     * Checks if a risk entry already exists for the provided asset, threat and
     * vulnerability combination.
     *
     * @param assetId         identifier of the asset
     * @param threatId        identifier of the threat
     * @param vulnerabilityId identifier of the vulnerability
     * @return {@code true} when a matching risk is present
     */
    boolean existsByAsset_IdAndThreat_IdAndVulnerability_Id(Long assetId, Long threatId, Long vulnerabilityId);
}

