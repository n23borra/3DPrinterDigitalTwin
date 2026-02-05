package com.fablab.backend.printer.polling;

import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.models.printer.PrinterSnapshot;
import com.fablab.backend.printer.connector.ConnectorRegistry;
import com.fablab.backend.printer.connector.PrinterConnector;
import com.fablab.backend.printer.connector.RawPrinterState;
import com.fablab.backend.repositories.printer.PrinterRepository;
import com.fablab.backend.repositories.printer.PrinterSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Background service that polls printers at intelligent intervals
 * and stores snapshots in the database.
 */
@Service
public class PrinterPollingService {

    private static final Logger log = LoggerFactory.getLogger(PrinterPollingService.class);

    private final PrinterRepository printerRepository;
    private final PrinterSnapshotRepository snapshotRepository;
    private final ConnectorRegistry connectorRegistry;

    public PrinterPollingService(
            PrinterRepository printerRepository,
            PrinterSnapshotRepository snapshotRepository,
            ConnectorRegistry connectorRegistry) {
        this.printerRepository = printerRepository;
        this.snapshotRepository = snapshotRepository;
        this.connectorRegistry = connectorRegistry;
    }

    /**
     * Poll all printers at a base rate (every 10 seconds)
     * This is for IDLE/OFFLINE printers - just checking if they're alive
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000) // 10s for idle printers
    public void pollIdlePrinters() {
        List<Printer> printers = printerRepository.findAll();
        
        for (Printer printer : printers) {
            try {
                // Get the appropriate connector
                PrinterConnector connector = connectorRegistry.resolve(printer.getType());
                
                // Fetch current state
                RawPrinterState state = connector.fetchState(printer);
                
                // Determine if printer is printing
                boolean isPrinting = isPrinting(state);
                
                // Only store snapshot for idle printers here
                // Printing printers are handled by the fast polling task
                if (!isPrinting) {
                    storeSnapshot(printer, state);
                    updatePrinterStatus(printer, state);
                }
                
            } catch (Exception e) {
                log.warn("Failed to poll printer {}: {}", printer.getName(), e.getMessage());
                // Mark printer as offline if we can't reach it
                printer.setStatus(com.fablab.backend.models.printer.PrinterStatus.OFFLINE);
                printer.setLastHeartbeat(Instant.now());
                printerRepository.save(printer);
            }
        }
    }

    /**
     * Fast polling for ACTIVE printers (every 1 second)
     * Only polls printers that are currently printing
     */
    @Scheduled(fixedDelay = 1000, initialDelay = 5000) // 1s for printing printers
    public void pollActivePrinters() {
        List<Printer> printers = printerRepository.findAll();
        
        for (Printer printer : printers) {
            try {
                PrinterConnector connector = connectorRegistry.resolve(printer.getType());
                RawPrinterState state = connector.fetchState(printer);
                
                // Only store snapshots for printers that are actively printing
                if (isPrinting(state)) {
                    storeSnapshot(printer, state);
                    updatePrinterStatus(printer, state);
                    log.debug("Fast poll: {} - Progress: {}%", 
                            printer.getName(), state.getProgress());
                }
                
            } catch (Exception e) {
                log.trace("Fast poll skipped for {}: {}", printer.getName(), e.getMessage());
            }
        }
    }

    /**
     * Check if printer is currently printing
     */
    private boolean isPrinting(RawPrinterState state) {
        if (state == null || state.getState() == null) {
            return false;
        }
        String printerState = state.getState().toLowerCase();
        return printerState.contains("printing") || 
               printerState.contains("paused") ||
               (state.getProgress() != null && state.getProgress() > 0 && state.getProgress() < 100);
    }

    /**
     * Store snapshot in database
     */
    private void storeSnapshot(Printer printer, RawPrinterState state) {
        PrinterSnapshot snapshot = PrinterSnapshot.builder()
                .printer(printer)
                .timestamp(state.getTimestamp() != null ? state.getTimestamp() : Instant.now())
                // Temperatures
                .bedTemp(state.getBedTemp())
                .targetBed(state.getTargetBed())
                .nozzleTemp(state.getNozzleTemp())
                .targetNozzle(state.getTargetNozzle())
                .chamberTemp(state.getChamberTemp())
                // Position
                .posX(state.getPosX())
                .posY(state.getPosY())
                .posZ(state.getPosZ())
                .posE(state.getPosE())
                .zHeight(state.getPosZ()) // Legacy field
                // Motion
                .homedAxes(state.getHomedAxes())
                .maxVelocity(state.getMaxVelocity())
                .maxAccel(state.getMaxAccel())
                .liveVelocity(state.getLiveVelocity())
                .livePositionX(state.getLivePositionX())
                .livePositionY(state.getLivePositionY())
                .livePositionZ(state.getLivePositionZ())
                // Print stats
                .state(state.getState())
                .filename(state.getFilename())
                .progress(state.getProgress())
                .totalLayers(state.getTotalLayers())
                .currentLayer(state.getCurrentLayer())
                .printDuration(state.getPrintDuration())
                .totalDuration(state.getTotalDuration())
                .filamentUsed(state.getFilamentUsed())
                .displayProgress(state.getDisplayProgress())
                // Fans & sensors
                .partFanSpeed(state.getPartFanSpeed())
                .partFanRPM(state.getPartFanRPM())
                .hotendFanSpeed(state.getHotendFanSpeed())
                .filamentDetected(state.getFilamentDetected())
                // System
                .cpuTemp(state.getCpuTemp())
                .cpuUsage(state.getCpuUsage())
                .memUsage(state.getMemUsage())
                .systemUptime(state.getSystemUptime())
                // Other
                .bedMeshProfile(state.getBedMeshProfile())
                .rawPayload(state.getRawPayload())
                .build();

        snapshotRepository.save(snapshot);
    }

 /**
 * Update printer status based on fetched state
 */
private void updatePrinterStatus(Printer printer, RawPrinterState state) {
    printer.setLastHeartbeat(Instant.now());
    
    if (state.getState() != null) {
        String stateStr = state.getState().toLowerCase();
        if (stateStr.contains("printing")) {
            printer.setStatus(com.fablab.backend.models.printer.PrinterStatus.PRINTING);
        } else if (stateStr.contains("paused")) {
            printer.setStatus(com.fablab.backend.models.printer.PrinterStatus.PAUSED);
        } else if (stateStr.contains("ready") || stateStr.contains("standby") || stateStr.contains("idle")) {
            printer.setStatus(com.fablab.backend.models.printer.PrinterStatus.IDLE);
        } else {
            // Default to IDLE if state is unknown but printer is responding
            printer.setStatus(com.fablab.backend.models.printer.PrinterStatus.IDLE);
        }
    } else {
        // No state received = printer not responding
        printer.setStatus(com.fablab.backend.models.printer.PrinterStatus.OFFLINE);
    }
    
    printerRepository.save(printer);
}

}