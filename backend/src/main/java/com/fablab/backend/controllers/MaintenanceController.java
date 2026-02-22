package com.fablab.backend.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fablab.backend.dto.AddMaintenanceHoursRequest;
import com.fablab.backend.dto.CreateMaintenanceItemRequest;
import com.fablab.backend.dto.MaintenanceItemDTO;
import com.fablab.backend.models.MaintenanceItem;
import com.fablab.backend.services.MaintenanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/maintenance/items")
@RequiredArgsConstructor
@Validated
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @GetMapping
    public List<MaintenanceItemDTO> getAll() {
        return maintenanceService.listAll().stream()
                .map(MaintenanceItemDTO::from)
                .toList();
    }

    @GetMapping("/{id}")
    public MaintenanceItemDTO getOne(@PathVariable Long id) {
        MaintenanceItem item = maintenanceService.getById(id);
        return MaintenanceItemDTO.from(item);
    }

    @PostMapping
    public ResponseEntity<MaintenanceItemDTO> create(@Valid @RequestBody CreateMaintenanceItemRequest request) {
        MaintenanceItem created = maintenanceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(MaintenanceItemDTO.from(created));
    }

    @PatchMapping("/{id}/hours")
    public MaintenanceItemDTO addHours(
            @PathVariable Long id,
            @Valid @RequestBody AddMaintenanceHoursRequest request) {
        MaintenanceItem updated = maintenanceService.addHours(id, request.hoursToAdd());
        return MaintenanceItemDTO.from(updated);
    }
}