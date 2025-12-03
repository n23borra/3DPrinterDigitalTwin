package com.fablab.backend.controllers;


import com.fablab.backend.dto.ControlAssignmentRequest;
import com.fablab.backend.dto.ControlLevelDTO;
import com.fablab.backend.dto.RiskControlDTO;
import com.fablab.backend.dto.RiskRequest;
import com.fablab.backend.models.Control;
import com.fablab.backend.models.ControlImplementation;
import com.fablab.backend.models.RiskBase;
import com.fablab.backend.models.RiskResult;
import com.fablab.backend.dto.RiskResultDTO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fablab.backend.models.enums.RiskStatus;
import com.cyber.backend.repositories.*;
import com.fablab.backend.repositories.*;
import com.fablab.backend.services.RiskCalculationService;
import com.fablab.backend.services.TreatmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risks")
@RequiredArgsConstructor
public class RiskController {

    private final AssetRepository assetRepo;
    private final ThreatRepository threatRepo;
    private final VulnerabilityRepository vulnRepo;
    private final RiskBaseRepository baseRepo;
    private final ControlRepository controlRepo;
    private final ControlImplementationRepository implRepo;
    private final ControlRecommendationRepository recommendationRepo;
    private final RiskCalculationService calcService;
    private final RiskResultRepository resultRepo;
    private final TreatmentPlanService planService;


    /**
     * Creates a new risk tuple (asset, threat, vulnerability) and triggers the initial calculation.
     *
     * @param req payload describing the asset, threat and vulnerability identifiers to combine
     * @return {@link ResponseEntity} containing the computed {@link RiskResultDTO}, or {@code 409 Conflict} if the triplet already exists
     */
    @PostMapping
    public ResponseEntity<RiskResultDTO> create(@RequestBody RiskRequest req) {
        if (baseRepo.existsByAsset_IdAndThreat_IdAndVulnerability_Id(
                req.assetId(), req.threatId(), req.vulnerabilityId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        RiskBase rb = new RiskBase();
        rb.setAsset(assetRepo.getReferenceById(req.assetId()));
        rb.setThreat(threatRepo.getReferenceById(req.threatId()));
        rb.setVulnerability(vulnRepo.getReferenceById(req.vulnerabilityId()));
        rb = baseRepo.save(rb);

        RiskResult rr = calcService.recalculate(rb);
        if (rr.getStatus() == RiskStatus.TREAT) {
            planService.createIfAbsent(rr);
        }
        return ResponseEntity.ok(RiskResultDTO.fromEntity(rr));
    }

    /**
     * Applies or updates a control implementation level for the specified risk and recalculates the result.
     *
     * @param dto payload identifying the risk, control and desired implementation level
     * @return {@link ResponseEntity} containing the recalculated {@link RiskResultDTO}
     */
    @PostMapping("/controls")
    public ResponseEntity<RiskResultDTO> applyControl(@RequestBody ControlLevelDTO dto) {
        RiskBase risk = baseRepo.getReferenceById(dto.riskId());
        Control ctrl = controlRepo.getReferenceById(dto.controlId());
        ControlImplementation ci = implRepo
                .findById(new ControlImplementation.PK(dto.riskId(), dto.controlId()))
                .orElseGet(() -> {
                    ControlImplementation created = new ControlImplementation();
                    created.setRisk(risk);
                    created.setControl(ctrl);
                    return created;
                });
        ci.setLevel(dto.level());
        implRepo.save(ci);
        RiskResult rr = calcService.recalculate(risk);
        if (rr.getStatus() == RiskStatus.TREAT) {
            planService.createIfAbsent(rr);
        }
        return ResponseEntity.ok(RiskResultDTO.fromEntity(rr));
    }

    /**
     * Removes a control implementation from a risk and triggers a recalculation.
     *
     * @param dto payload identifying the risk and control to detach
     * @return {@link ResponseEntity} with the updated {@link RiskResultDTO}, or {@code 404 Not Found} if the risk is unknown
     */
    @DeleteMapping("/controls")
    public ResponseEntity<RiskResultDTO> removeControl(@RequestBody ControlAssignmentRequest dto) {
        return baseRepo.findById(dto.riskId())
                .map(risk -> {
                    ControlImplementation.PK pk = new ControlImplementation.PK(dto.riskId(), dto.controlId());
                    implRepo.findById(pk).ifPresent(implRepo::delete);
                    RiskResult rr = calcService.recalculate(risk);
                    if (rr.getStatus() == RiskStatus.TREAT) {
                        planService.createIfAbsent(rr);
                    }
                    return ResponseEntity.ok(RiskResultDTO.fromEntity(rr));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lists the controls currently applied to a risk.
     *
     * @param riskId identifier of the risk base
     * @return {@link ResponseEntity} containing the applied controls with their implementation levels
     */
    @GetMapping("/{riskId}/controls")
    public ResponseEntity<List<RiskControlDTO>> controls(@PathVariable Long riskId) {
        List<RiskControlDTO> dtos = implRepo.findByRisk_Id(riskId)
                .stream().map(RiskControlDTO::fromEntity).toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Suggests controls based on the threat and vulnerability linked to a risk.
     *
     * @param riskId identifier of the risk base for which recommendations are requested
     * @return {@link ResponseEntity} containing recommended controls with their current implementation levels,
     *         or {@code 404 Not Found} if the risk cannot be located
     */
    @GetMapping("/{riskId}/recommended-controls")
    public ResponseEntity<List<RiskControlDTO>> recommended(@PathVariable Long riskId) {
        return baseRepo.findById(riskId)
                .map(rb -> {
                    Map<Long, Double> levels = implRepo.findByRisk_Id(riskId)
                            .stream()
                            .collect(Collectors.toMap(ci -> ci.getControl().getId(), ControlImplementation::getLevel));
                    List<RiskControlDTO> dtos = recommendationRepo
                            .findAllByThreat_IdAndVulnerability_Id(
                                    rb.getThreat().getId(),
                                    rb.getVulnerability().getId())
                            .stream()
                            .map(rec -> {
                                Long cid = rec.getControl().getId();
                                double lvl = levels.getOrDefault(cid, 0.0);
                                return new RiskControlDTO(cid, rec.getControl().getLabel(), lvl);
                            })
                            .toList();
                    return ResponseEntity.ok(dtos);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lists all risks and their latest calculations for the specified analysis.
     *
     * @param analysisId identifier of the analysis whose risks should be returned
     * @return {@link ResponseEntity} containing a list of {@link RiskResultDTO} enriched with risk metadata
     */
    @GetMapping
    public ResponseEntity<List<RiskResultDTO>> list(@RequestParam Long analysisId) {
        List<RiskResultDTO> dtos = resultRepo.findAllByRisk_Asset_Analysis_Id(analysisId)
                .stream()
                .map(rr -> RiskResultDTO.fromEntity(rr, rr.getRisk()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Deletes a risk base and any associated calculations.
     *
     * @param id identifier of the risk base to delete
     * @return {@link ResponseEntity} with {@code 200 OK} once the risk has been removed
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        baseRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
