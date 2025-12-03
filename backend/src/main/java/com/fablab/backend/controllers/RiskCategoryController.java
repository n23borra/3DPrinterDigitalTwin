package com.fablab.backend.controllers;

import com.fablab.backend.models.Threat;
import com.fablab.backend.repositories.ThreatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/risk-categories")
@RequiredArgsConstructor
public class RiskCategoryController {

    private final ThreatRepository threatRepo;

    /**
     * Lists all threat categories to support risk definition.
     *
     * @return {@link ResponseEntity} containing the list of {@link Threat} entries
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Threat>> list() {
        return ResponseEntity.ok(threatRepo.findAll());
    }
}