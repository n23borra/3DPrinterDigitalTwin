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
@Table(name = "print_jobs")
public class PrintJob {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "printer_id", nullable = false)
    private Printer printer;

    private String fileName;
    private String material;
    private Instant startedAt;
    private Instant endedAt;
    private Long durationSec;

    @Enumerated(EnumType.STRING)
    private PrintJobStatus status;

    @Lob
    private String notes;
}