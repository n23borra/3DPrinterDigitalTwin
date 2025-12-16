package com.fablab.backend.controllers;

import com.fablab.backend.dto.AuditLogDTO;
import com.fablab.backend.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditRepo;

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
}