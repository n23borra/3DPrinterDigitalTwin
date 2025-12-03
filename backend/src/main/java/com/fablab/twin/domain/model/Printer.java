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
@Table(name = "printers")
public class Printer {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrinterType type;

    private String ip;

    private Integer port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrinterStatus status;

    private Instant lastHeartbeat;

    private String firmware;

    @Lob
    private String metadata;
}