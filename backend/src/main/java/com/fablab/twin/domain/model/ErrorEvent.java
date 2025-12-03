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
@Table(name = "error_events")
public class ErrorEvent
{
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "printer_id", nullable = false)
    private Printer printer;

    @Enumerated(EnumType.STRING)
    private ErrorSeverity severity;

    @Enumerated(EnumType.STRING)
    private ErrorType type;

    @Column(nullable = false)
    private String message;

    @Lob
    private String details;

    @Column(nullable = false)
    private Instant createdAt;

    private boolean acknowledged;
}