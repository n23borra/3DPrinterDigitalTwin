package com.fablab.backend.controllers;

import com.fablab.backend.dto.TreatmentPlanCreateDTO;
import com.fablab.backend.dto.TreatmentPlanUpdateDTO;
import com.fablab.backend.models.TreatmentPlan;
import com.fablab.backend.services.TreatmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/treatments")
@RequiredArgsConstructor
public class TreatmentPlanController {

    private final TreatmentPlanService service;

    /**
     * Creates a treatment plan for the provided risk result.
     *
     * @param dto payload containing the risk result identifier and the strategy to apply
     * @return {@link ResponseEntity} containing the persisted {@link TreatmentPlan}
     */
    @PostMapping
    public ResponseEntity<TreatmentPlan> create(@RequestBody TreatmentPlanCreateDTO dto) {
        return ResponseEntity.ok(service.create(dto.riskResultId(), dto.strategy()));
    }

    /**
     * Lists the treatment plans associated with the risks of a given analysis.
     *
     * @param analysisId identifier of the analysis whose treatment plans should be returned
     * @return {@link ResponseEntity} containing the collection of treatment plans
     */
    @GetMapping
    public ResponseEntity<?> list(@RequestParam Long analysisId) {
        return ResponseEntity.ok(service.listByAnalysis(analysisId));
    }

    /**
     * Retrieves a treatment plan by its identifier.
     *
     * @param id identifier of the treatment plan to fetch
     * @return {@link ResponseEntity} containing the requested {@link TreatmentPlan}
     */
    @GetMapping("{id}")
    public ResponseEntity<TreatmentPlan> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    /**
     * Updates a treatment plan with the provided attributes.
     *
     * @param id identifier of the treatment plan to update
     * @param dto payload containing the new description, assignee, due date, strategy or status
     * @return {@link ResponseEntity} containing the updated {@link TreatmentPlan}
     */
    @PutMapping("{id}")
    public ResponseEntity<TreatmentPlan> update(@PathVariable Long id, @RequestBody TreatmentPlanUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
}