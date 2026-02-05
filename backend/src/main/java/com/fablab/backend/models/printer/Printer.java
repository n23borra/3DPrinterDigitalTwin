package com.fablab.backend.models.printer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a physical printer reachable by a connector implementation.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "printers")
public class Printer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrinterType type;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column
    private Integer port;

    @Column(name = "api_key")
    private String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrinterStatus status = PrinterStatus.OFFLINE;

    private Instant lastHeartbeat;

    private String firmware;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}