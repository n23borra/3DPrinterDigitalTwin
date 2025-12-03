package com.fablab.twin.service;

import com.fablab.twin.domain.model.Printer;
import com.fablab.twin.domain.model.PrinterSnapshot;
import com.fablab.twin.repository.PrinterRepository;
import com.fablab.twin.repository.PrinterSnapshotRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PrinterService {

    private final PrinterRepository printerRepository;
    private final PrinterSnapshotRepository snapshotRepository;

    public PrinterService(PrinterRepository printerRepository, PrinterSnapshotRepository snapshotRepository) {
        this.printerRepository = printerRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public List<Printer> findAll() {
        return printerRepository.findAll();
    }

    public Printer save(Printer printer) {
        return printerRepository.save(printer);
    }

    public PrinterSnapshot saveSnapshot(PrinterSnapshot snapshot) {
        snapshot.setTimestamp(Instant.now());
        return snapshotRepository.save(snapshot);
    }

    public List<PrinterSnapshot> findSnapshots(UUID printerId) {
        return snapshotRepository.findTop50ByPrinterIdOrderByTimestampDesc(printerId);
    }
}