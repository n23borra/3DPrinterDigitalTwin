package com.fablab.twin.service;

import com.fablab.twin.domain.model.Printer;
import com.fablab.twin.domain.model.PrinterSnapshot;
import com.fablab.twin.domain.model.PrinterStatus;
import com.fablab.twin.printer.connector.PrinterConnector;
import com.fablab.twin.service.dto.RawPrinterState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PrinterPollingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrinterPollingService.class);

    private final PrinterService printerService;
    private final List<PrinterConnector> connectors;

    public PrinterPollingService(PrinterService printerService, List<PrinterConnector> connectors) {
        this.printerService = printerService;
        this.connectors = connectors;
    }

    @Scheduled(fixedDelayString = "${polling.interval-ms:5000}")
    public void pollPrinters() {
        printerService.findAll().forEach(this::pollPrinter);
    }

    private void pollPrinter(Printer printer) {
        connectors.stream()
                .filter(connector -> connector.supports(printer))
                .findFirst()
                .ifPresent(connector -> {
                    try {
                        RawPrinterState raw = connector.fetchState(printer);
                        String rawState = raw.state() != null ? raw.state().toUpperCase() : "UNKNOWN";
                        PrinterSnapshot snapshot = PrinterSnapshot.builder()
                                .printer(printer)
                                .timestamp(raw.timestamp() != null ? raw.timestamp() : Instant.now())
                                .bedTemp(raw.bedTemp())
                                .nozzleTemp(raw.nozzleTemp())
                                .targetBed(raw.targetBed())
                                .targetNozzle(raw.targetNozzle())
                                .progress(raw.progress())
                                .layer(raw.layer())
                                .zHeight(raw.zHeight())
                                .state(PrinterStatus.valueOf(rawState))
                                .rawPayload(raw.rawPayload())
                                .build();
                        printer.setLastHeartbeat(Instant.now());
                        printer.setStatus(snapshot.getState());
                        printerService.save(printer);
                        printerService.saveSnapshot(snapshot);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to poll printer {}: {}", printer.getName(), e.getMessage());
                        printer.setStatus(PrinterStatus.OFFLINE);
                        printerService.save(printer);
                    }
                });
    }
}