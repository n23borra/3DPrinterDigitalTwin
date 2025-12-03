package com.fablab.twin.repository;

import com.fablab.twin.domain.model.PrinterSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PrinterSnapshotRepository extends JpaRepository<PrinterSnapshot, UUID> {
    List<PrinterSnapshot> findTop50ByPrinterIdOrderByTimestampDesc(UUID printerId);
    List<PrinterSnapshot> findByTimestampAfter(Instant timestamp);
}