package com.fablab.backend.controllers;


import com.fablab.backend.dto.AnalysisRequest;
import com.fablab.backend.models.Analysis;
import com.fablab.backend.repositories.AnalysisRepository;
import com.fablab.backend.repositories.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisRepository repo;
    private final UserRepository userRepo;

    /**
     * Retrieves all analyses, optionally filtered by owner identifier.
     *
     * @param ownerId optional owner identifier used to filter the analyses; when {@code null} every analysis is returned
     * @return {@link ResponseEntity} containing the full list or the filtered subset of analyses
     */
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long ownerId) {
        if (ownerId != null) {
            return ResponseEntity.ok(repo.findAllByOwnerId(ownerId));
        }
        return ResponseEntity.ok(repo.findAll());
    }

    /**
     * Creates a new analysis for the authenticated super administrator, applying default thresholds based on criticality.
     *
     * @param req validated payload describing the analysis to create
     * @param authentication security context used to resolve the owner identifier
     * @return {@link ResponseEntity} containing the persisted analysis definition
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Analysis> create(@RequestBody @Valid AnalysisRequest req, Authentication authentication) {

        Long ownerId = userRepo.findByUsername(authentication.getName()).orElseThrow().getId();

        int[] preset = switch (req.criticality()) {
            case LOW -> new int[]{30, 60};
            case MEDIUM -> new int[]{20, 50};
            case HIGH -> new int[]{10, 25};
        };

        Analysis a = new Analysis();
        a.setName(req.name());
        a.setDescription(req.description());
        a.setLanguage(req.language());
        a.setScope(req.scope());
        a.setCriticality(req.criticality());
        a.setS1(preset[0]);
        a.setS2(preset[1]);
        a.setDm(req.dm());
        a.setTa(req.ta());
        a.setOwnerId(ownerId);

        return ResponseEntity.ok(repo.save(a));
    }

    /**
     * Fetches a single analysis by its identifier.
     *
     * @param id analysis identifier to retrieve
     * @return {@link ResponseEntity} with the matching analysis or {@code 404 Not Found} when it does not exist
     */
    @GetMapping("{id}")
    public ResponseEntity<Analysis> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing analysis with the provided payload.
     *
     * @param id analysis identifier to update
     * @param req validated request body carrying the new analysis attributes
     * @return {@link ResponseEntity} containing the updated analysis or {@code 404 Not Found} if the analysis is missing
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Analysis> update(@PathVariable Long id, @RequestBody @Valid AnalysisRequest req) {
        var aOpt = repo.findById(id);
        if (aOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        int[] preset = switch (req.criticality()) {
            case LOW -> new int[]{30, 60};
            case MEDIUM -> new int[]{20, 50};
            case HIGH -> new int[]{10, 25};
        };

        Analysis a = aOpt.get();
        a.setName(req.name());
        a.setDescription(req.description());
        a.setLanguage(req.language());
        a.setScope(req.scope());
        a.setCriticality(req.criticality());
        a.setS1(preset[0]);
        a.setS2(preset[1]);
        a.setDm(req.dm());
        a.setTa(req.ta());
        return ResponseEntity.ok(repo.save(a));
    }

    /**
     * Deletes an analysis by identifier.
     *
     * @param id analysis identifier to remove
     * @return {@link ResponseEntity} with {@code 200 OK} when the deletion succeeds or {@code 404 Not Found} if the analysis does not exist
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
