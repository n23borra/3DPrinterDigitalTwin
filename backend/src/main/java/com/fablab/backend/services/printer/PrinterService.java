package com.fablab.backend.services.printer;

import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.models.printer.PrinterSnapshot;
import com.fablab.backend.models.printer.PrinterStatus;
import com.fablab.backend.printer.connector.ConnectorRegistry;
import com.fablab.backend.printer.connector.PrinterConnector;
import com.fablab.backend.printer.connector.RawPrinterState;
import com.fablab.backend.repositories.printer.PrinterRepository;
import com.fablab.backend.repositories.printer.PrinterSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Coordinates printer operations between repositories and connectors.
 */
@Service
@RequiredArgsConstructor
public class PrinterService {

    private static final Logger log = LoggerFactory.getLogger(PrinterService.class);

    private final PrinterRepository printerRepository;
    private final PrinterSnapshotRepository snapshotRepository;
    private final ConnectorRegistry connectorRegistry;

    public List<Printer> listPrinters() {
        return printerRepository.findAll();
    }

    public Printer getPrinter(UUID id) {
        return printerRepository.findById(id).orElseThrow();
    }

    @Transactional
    public PrinterSnapshot fetchAndPersistSnapshot(UUID printerId) {
        Printer printer = getPrinter(printerId);
        PrinterConnector connector = connectorRegistry.resolve(printer.getType());
        RawPrinterState raw = connector.fetchState(printer);

        printer.setLastHeartbeat(raw.getTimestamp());
        printer.setStatus(resolveStatus(raw.getState()));
        printerRepository.save(printer);

        PrinterSnapshot snapshot = PrinterSnapshot.builder()
                .printer(printer)
                .timestamp(raw.getTimestamp())
                .bedTemp(raw.getBedTemp())
                .nozzleTemp(raw.getNozzleTemp())
                .targetBed(raw.getTargetBed())
                .targetNozzle(raw.getTargetNozzle())
                .progress(raw.getProgress())
                .zHeight(raw.getZHeight())
                .state(raw.getState())
                .rawPayload(raw.getRawPayload())
                .build();
        return snapshotRepository.save(snapshot);
    }

    public List<PrinterSnapshot> getHistory(UUID printerId, Instant from, Instant to) {
        if (from != null && to != null) {
            return snapshotRepository.findByPrinterIdAndTimestampBetweenOrderByTimestampDesc(printerId, from, to);
        }
        return snapshotRepository.findTop50ByPrinterIdOrderByTimestampDesc(printerId);
    }

    public PrinterSnapshot getLatestStoredSnapshot(UUID printerId) {
        return snapshotRepository.findFirstByPrinterIdOrderByTimestampDesc(printerId)
                .orElseThrow();
    }

    public void sendCommand(UUID printerId, String command) {
        Printer printer = getPrinter(printerId);
        PrinterConnector connector = connectorRegistry.resolve(printer.getType());
        connector.sendCommand(printer, command);
        log.info("Sent command {} to printer {}", command, printer.getName());
    }

    private PrinterStatus resolveStatus(String rawState) {
        if (rawState == null) {
            return PrinterStatus.OFFLINE;
        }
        return switch (rawState.toUpperCase()) {
            case "PRINTING" -> PrinterStatus.PRINTING;
            case "PAUSED" -> PrinterStatus.PAUSED;
            default -> PrinterStatus.IDLE;
        };
    }
}