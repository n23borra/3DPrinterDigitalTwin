package com.fablab.twin.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "maintenance_rules")
public class MaintenanceRule {
    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrinterType printerType;

    @Column(nullable = false)
    private String trigger;

    @Column(nullable = false)
    private Double threshold;

    private String description;
    private String actionHint;
}