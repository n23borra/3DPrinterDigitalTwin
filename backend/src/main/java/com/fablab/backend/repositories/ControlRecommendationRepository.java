package com.fablab.backend.repositories;

import com.fablab.backend.models.ControlRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository returning recommended controls for threat and vulnerability
 * combinations.
 */
public interface ControlRecommendationRepository extends JpaRepository<ControlRecommendation, Long> {
    /**
     * Finds all recommendations matching the provided threat and vulnerability.
     *
     * @param threatId        identifier of the threat
     * @param vulnerabilityId identifier of the vulnerability
     * @return recommended controls covering the pair
     */
    List<ControlRecommendation> findAllByThreat_IdAndVulnerability_Id(Long threatId, Long vulnerabilityId);
}