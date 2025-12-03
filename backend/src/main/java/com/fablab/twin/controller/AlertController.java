package com.fablab.twin.controller;

import com.fablab.twin.domain.model.ErrorEvent;
import com.fablab.twin.domain.model.ErrorSeverity;
import com.fablab.twin.domain.model.ErrorType;
import com.fablab.twin.service.AlertService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<ErrorEvent> list() {
        return alertService.listRecent();
    }

    @PostMapping
    public ErrorEvent create(@RequestBody Map<String, String> payload) {
        UUID printerId = UUID.fromString(payload.get("printerId"));
        ErrorSeverity severity = ErrorSeverity.valueOf(payload.getOrDefault("severity", "INFO"));
        ErrorType type = ErrorType.valueOf(payload.getOrDefault("type", "UNKNOWN"));
        String message = payload.getOrDefault("message", "");
        String details = payload.getOrDefault("details", "");
        return alertService.create(printerId, severity, type, message, details);
    }

    @PostMapping("/{id}/ack")
    public ResponseEntity<Void> ack(@PathVariable UUID id) {
        alertService.acknowledge(id);
        return ResponseEntity.noContent().build();
    }
}