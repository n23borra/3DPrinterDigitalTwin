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
@Table(name = "printer_snapshots")
public class PrinterSnapshot {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "printer_id", nullable = false)
    private Printer printer;

    @Column(nullable = false)
    private Instant timestamp;

    private Double bedTemp;
    private Double nozzleTemp;
    private Double targetBed;
    private Double targetNozzle;
    private Double progress;
    private Integer layer;
    private Double zHeight;

    @Enumerated(EnumType.STRING)
    private PrinterStatus state;

    @Lob
    private String rawPayload;
}