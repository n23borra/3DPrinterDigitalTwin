package com.fablab.backend.controllers;

import com.fablab.backend.models.Threat;
import com.fablab.backend.repositories.ThreatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/threats")
@RequiredArgsConstructor
public class ThreatController {

    private final ThreatRepository threatRepo;

    /**
     * Retrieves all threats available for risk modelling.
     *
     * @return {@link ResponseEntity} containing the list of {@link Threat} records
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Threat>> list() {
        return ResponseEntity.ok(threatRepo.findAll());
    }

    /**
     * Creates a new threat entry.
     *
     * @param threat payload describing the threat to persist; its identifier is reset to enforce creation
     * @return {@link ResponseEntity} containing the stored {@link Threat}
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Threat> create(@RequestBody Threat threat) {
        threat.setId(null);
        return ResponseEntity.ok(threatRepo.save(threat));
    }

    /**
     * Updates the attributes of an existing threat.
     *
     * @param id identifier of the threat to update
     * @param data payload carrying the new label and probability values
     * @return {@link ResponseEntity} containing the updated {@link Threat}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Threat> update(@PathVariable Long id, @RequestBody Threat data) {
        Threat t = threatRepo.findById(id).orElseThrow();
        t.setLabel(data.getLabel());
        t.setProbability(data.getProbability());
        return ResponseEntity.ok(threatRepo.save(t));
    }

    /**
     * Deletes a threat by its identifier.
     *
     * @param id identifier of the threat to delete
     * @return {@link ResponseEntity} with {@code 200 OK} after the threat has been removed
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        threatRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}