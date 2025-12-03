package com.fablab.backend.repositories;

import com.fablab.backend.models.TreatmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository managing {@link TreatmentPlan} records.
 */
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Long> {
    /**
     * Finds the treatment plan assigned to a specific risk result.
     *
     * @param riskResultId identifier of the risk result
     * @return the plan if one has been created
     */
    Optional<TreatmentPlan> findByRiskResult_Id(Long riskResultId);

    /**
     * Lists treatment plans for every risk result contained in the given analysis.
     *
     * @param analysisId identifier of the analysis
     * @return matching treatment plans
     */
    List<TreatmentPlan> findAllByRiskResult_Risk_Asset_Analysis_Id(Long analysisId);
}
