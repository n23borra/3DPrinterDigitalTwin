package com.fablab.twin.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "maintenance_tasks")
public class MaintenanceTask {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "printer_id", nullable = false)
    private Printer printer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private MaintenanceRule rule;

    @Column(nullable = false)
    private Instant dueAt;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private String note;
}