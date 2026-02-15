package com.fablab.backend.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fablab.backend.dto.AuditLogDTO;
import com.fablab.backend.repositories.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditRepo;

    public static record CreateAuditRequest(Long userId, String action, String details) {}

    /**
     * Retrieves audit events recorded for the specified user.
     *
     * @param userId identifier of the user whose audit logs should be returned
     * @return list of {@link AuditLogDTO} entries ordered by persistence order
     */
    @GetMapping
    public List<AuditLogDTO> logs(@RequestParam Long userId) {
        return auditRepo.findAllByUserId(userId)
                .stream()
                .map(AuditLogDTO::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<AuditLogDTO> create(@RequestBody CreateAuditRequest req) {
        var log = new com.fablab.backend.models.AuditLog();
        log.setUserId(req.userId());
        log.setAction(req.action());
        log.setDetails(req.details());
        var saved = auditRepo.save(log);
        return ResponseEntity.ok(AuditLogDTO.from(saved));
    }
}