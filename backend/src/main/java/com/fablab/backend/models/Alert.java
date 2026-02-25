package com.fablab.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who created/owns this alert (nullable for system-generated alerts)
    private Long userId;

    // Printer from which the alert came
    @Column(nullable = false)
    private UUID printerId;

    // Alert title/name
    @Column(nullable = false)
    private String title;

    // Detailed description
    @Column(columnDefinition = "text")
    private String details;

    // Timestamp when alert was created (auto-set by Hibernate)
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant logTime = Instant.now();

    // Whether the alert has been resolved/repaired
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.UNRESOLVED;

    // Severity level: INFO, WARNING, CRITICAL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity = Severity.INFO;

    // Priority: LOW, MEDIUM, HIGH
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    // Category/type of alert: HEATBED, AXIS, POWER, EXTRUDER, etc.
    @Column(length = 50)
    private String category;

    // User assigned to fix this alert (nullable if unassigned)
    private Long assignedTo;

    public enum Severity {
        INFO, WARNING, CRITICAL
    }

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    public enum Status {
        UNRESOLVED, IN_PROGRESS, RESOLVED
    }
}
