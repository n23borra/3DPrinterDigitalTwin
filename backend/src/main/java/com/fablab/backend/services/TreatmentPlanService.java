package com.fablab.backend.services;

import com.fablab.backend.dto.TreatmentPlanUpdateDTO;
import com.fablab.backend.models.RiskResult;
import com.fablab.backend.models.TreatmentPlan;
import com.fablab.backend.models.enums.ActionStatus;
import com.fablab.backend.models.enums.TreatmentStrategy;
import com.fablab.backend.repositories.RiskResultRepository;
import com.fablab.backend.repositories.TreatmentPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TreatmentPlanService {

    private final TreatmentPlanRepository planRepo;
    private final RiskResultRepository resultRepo;

    /**
     * Creates a treatment plan for a risk result.
     *
     * @param riskResultId identifier of the risk result to attach to the plan
     * @param strategy     treatment strategy to apply
     * @return the persisted {@link TreatmentPlan}
     */
    public TreatmentPlan create(Long riskResultId, TreatmentStrategy strategy) {
        RiskResult rr = resultRepo.findById(riskResultId).orElseThrow();
        TreatmentPlan plan = new TreatmentPlan();
        plan.setRiskResult(rr);
        plan.setStrategy(strategy);
        return planRepo.save(plan);
    }

    /**
     * Retrieves a treatment plan by its identifier.
     *
     * @param id identifier of the plan to retrieve
     * @return the matching {@link TreatmentPlan}
     */
    public TreatmentPlan get(Long id) {
        return planRepo.findById(id).orElseThrow();
    }

    /**
     * Lists treatment plans linked to the risks of the specified analysis.
     *
     * @param analysisId identifier of the analysis whose plans should be returned
     * @return list of matching {@link TreatmentPlan} entities
     */
    public List<TreatmentPlan> listByAnalysis(Long analysisId) {
        return planRepo.findAllByRiskResult_Risk_Asset_Analysis_Id(analysisId);
    }

    /**
     * Updates a treatment plan with the provided details.
     *
     * @param id  identifier of the plan to update
     * @param dto update payload containing the new properties
     * @return the updated {@link TreatmentPlan}
     */
    public TreatmentPlan update(Long id, TreatmentPlanUpdateDTO dto) {
        TreatmentPlan plan = planRepo.findById(id).orElseThrow();
        if (dto.description() != null) {
            plan.setDescription(dto.description());
        }
        if (dto.responsibleId() != null) {
            plan.setResponsibleId(dto.responsibleId());
        }
        if (dto.dueDate() != null) {
            plan.setDueDate(dto.dueDate());
        }
        if (dto.strategy() != null) {
            plan.setStrategy(dto.strategy());
        }
        if (dto.status() != null) {
            plan.setStatus(dto.status());
            if (dto.status() == ActionStatus.DONE) {
                plan.setClosedAt(Instant.now());
            }
        }
        return planRepo.save(plan);
    }

    /**
     * Ensures that a treatment plan exists for the provided risk result, creating one when missing.
     *
     * @param rr risk result that should have an associated treatment plan
     */
    public void createIfAbsent(RiskResult rr) {
        planRepo.findByRiskResult_Id(rr.getId()).orElseGet(() -> {
            TreatmentPlan tp = new TreatmentPlan();
            tp.setRiskResult(rr);
            tp.setStrategy(TreatmentStrategy.MITIGATE);
            return planRepo.save(tp);
        });
    }
}