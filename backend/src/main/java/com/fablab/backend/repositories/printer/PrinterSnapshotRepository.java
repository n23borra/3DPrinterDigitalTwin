package com.fablab.backend.repositories.printer;

import com.fablab.backend.models.printer.PrinterSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrinterSnapshotRepository extends JpaRepository<PrinterSnapshot, Long> {
    Optional<PrinterSnapshot> findFirstByPrinterIdOrderByTimestampDesc(UUID printerId);

    List<PrinterSnapshot> findByPrinterIdAndTimestampBetweenOrderByTimestampDesc(UUID printerId, Instant from, Instant to);

    List<PrinterSnapshot> findTop50ByPrinterIdOrderByTimestampDesc(UUID printerId);
}