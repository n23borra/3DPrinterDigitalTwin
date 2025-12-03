package com.fablab.backend.dto;

import com.fablab.backend.models.AuditLog;

import java.time.Instant;

/**
 * DTO exposing audit log entries for display in the UI.
 *
 * @param id       identifier of the persisted audit event
 * @param action   high-level action label stored in the log
 * @param details  optional contextual details recorded for the action
 * @param logTime  timestamp at which the log entry was created
 * @param message  convenience message combining the action and details
 */
public record AuditLogDTO(Long id, String action, String details, Instant logTime, String message) {
    public static AuditLogDTO from(AuditLog log) {
        String msg = log.getAction();
        if (log.getDetails() != null && !log.getDetails().isBlank()) {
            msg += " - " + log.getDetails();
        }
        return new AuditLogDTO(log.getId(), log.getAction(), log.getDetails(), log.getLogTime(), msg);
    }
}