package com.fablab.backend.repositories;

import com.fablab.backend.models.RiskResult;
import com.fablab.backend.models.RiskBase;
import com.fablab.backend.models.enums.RiskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository exposing computed {@link RiskResult} projections.
 */
public interface RiskResultRepository extends JpaRepository<RiskResult, Long> {

    /**
     * Retrieves the risk result linked to a particular {@link RiskBase}.
     *
     * @param riskId identifier of the risk base
     * @return optional containing the matching result when present
     */
    Optional<RiskResult> findByRisk_Id(Long riskId);

    /**
     * Lists every risk result belonging to the specified analysis.
     *
     * @param analysisId identifier of the analysis
     * @return risk results for assets within that analysis
     */
    List<RiskResult> findAllByRisk_Asset_Analysis_Id(Long analysisId);

    /**
     * Retrieves risk results filtered by workflow status.
     *
     * @param status status to filter on
     * @return matching risk results
     */
    List<RiskResult> findAllByStatus(RiskStatus status);
}



