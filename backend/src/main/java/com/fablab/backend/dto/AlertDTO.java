package com.fablab.backend.dto;

import java.time.Instant;

import com.fablab.backend.models.Alert;

/**
 * DTO for Alert entries.
 *
 * @param id identifier of the alert
 * @param userId id of the user who created the alert (nullable)
 * @param title alert title
 * @param details alert description
 * @param logTime timestamp when alert was created
 * @param resolved whether the alert has been resolved
 * @param severity severity level (INFO, WARNING, CRITICAL)
 * @param priority priority level (LOW, MEDIUM, HIGH)
 * @param category category/type of alert
 * @param assignedTo id of user assigned to fix (nullable)
 */
public record AlertDTO(Long id, Long userId, String title, String details, Instant logTime, 
                       boolean resolved, String severity, String priority, String category, Long assignedTo) {
    
    public static AlertDTO from(Alert alert) {
        return new AlertDTO(
            alert.getId(),
            alert.getUserId(),
            alert.getTitle(),
            alert.getDetails(),
            alert.getLogTime(),
            alert.isResolved(),
            alert.getSeverity().name(),
            alert.getPriority().name(),
            alert.getCategory(),
            alert.getAssignedTo()
        );
    }
}
