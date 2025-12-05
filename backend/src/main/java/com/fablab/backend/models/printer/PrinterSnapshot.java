package com.fablab.backend.models.printer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Historical normalized snapshot captured from a printer connector.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "printer_snapshots")
public class PrinterSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "printer_id")
    private Printer printer;

    @Column(name = "ts", nullable = false)
    private Instant timestamp = Instant.now();

    @Column(name = "bed_temp")
    private Double bedTemp;

    @Column(name = "nozzle_temp")
    private Double nozzleTemp;

    @Column(name = "target_bed")
    private Double targetBed;

    @Column(name = "target_nozzle")
    private Double targetNozzle;

    private Double progress;

    @Column(name = "z_height")
    private Double zHeight;

    private String state;

    @Column(columnDefinition = "jsonb")
    private String rawPayload;
}