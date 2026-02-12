package com.fablab.backend.services;

import org.springframework.stereotype.Service;

import com.fablab.backend.models.AuditLog;
import com.fablab.backend.repositories.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditRepo;

    /**
     * Persists an audit entry describing an action performed by a user.
     *
     * @param userId  identifier of the user who triggered the action
     * @param action  short action code to record
     * @param details optional contextual details to store alongside the action
     */
    public void logAction(Long userId, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setDetails(details);
        auditRepo.save(log);
    }
}