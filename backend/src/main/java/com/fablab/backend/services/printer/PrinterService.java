package com.fablab.backend.services.printer;

import com.fablab.backend.dto.PrinterCommandType;
import com.fablab.backend.models.User;
import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.models.printer.PrinterSnapshot;
import com.fablab.backend.models.printer.PrinterStatus;
import com.fablab.backend.models.printer.PrinterType;
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
import java.util.Set;
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
    public Printer createPrinter(String name, PrinterType type, String ipAddress, Integer port, String apiKey) {
        Printer printer = Printer.builder()
                .name(name)
                .type(type)
                .ipAddress(ipAddress)
                .port(port)
                .apiKey(apiKey)
                .status(PrinterStatus.OFFLINE)
                .build();
        return printerRepository.save(printer);
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
                .bedMeshProfile(raw.getBedMeshProfile())
                .bedMeshMin(raw.getBedMeshMin())
                .bedMeshMax(raw.getBedMeshMax())
                .bedMeshMatrix(raw.getBedMeshMatrix())
                .zTiltApplied(raw.getZTiltApplied())
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

    /**
     * G-code commands that can affect the printer hardware in dangerous ways.
     * Blocked for regular users — only ADMIN and SUPER_ADMIN may send these.
     */
    private static final Set<String> DANGEROUS_GCODES = Set.of(
            "M104",                    // set nozzle temperature
            "M109",                    // set nozzle temp and wait
            "M140",                    // set bed temperature
            "M190",                    // set bed temp and wait
            "M141",                    // set chamber temperature
            "M191",                    // set chamber temp and wait
            "SET_HEATER_TEMPERATURE",  // Klipper heater command
            "FORCE_MOVE",              // bypass kinematics — can crash axes
            "RESTART",                 // restart Klipper host
            "FIRMWARE_RESTART",        // restart firmware
            "SAVE_CONFIG"              // overwrite config and restart
    );

    public void sendCommand(UUID printerId, PrinterCommandType type, String payload, User.Role role) {
        Printer printer = getPrinter(printerId);

        // --- Validate action commands against printer status ---
        validatePrinterStatus(type, printer);

        // --- Validate dangerous G-code against user role ---
        if (type == PrinterCommandType.GCODE) {
            validateGcode(payload, role);
        }

        PrinterConnector connector = connectorRegistry.resolve(printer.getType());
        connector.sendCommand(printer, type, payload);
        log.info("Sent {} command to printer {} (role={})", type, printer.getName(), role);
    }

    /**
     * Checks that the printer is in the right state for the requested action.
     * EMERGENCY_STOP, FIRMWARE_RESTART and MACHINE_REBOOT are always allowed.
     */
    private void validatePrinterStatus(PrinterCommandType type, Printer printer) {
        PrinterStatus status = printer.getStatus();

        switch (type) {
            case PRINT_START -> {
                if (status != PrinterStatus.IDLE) {
                    throw new IllegalStateException(
                            "Cannot start print: printer is " + status + " (must be IDLE)");
                }
            }
            case PRINT_PAUSE -> {
                if (status != PrinterStatus.PRINTING) {
                    throw new IllegalStateException(
                            "Cannot pause: printer is " + status + " (must be PRINTING)");
                }
            }
            case PRINT_RESUME -> {
                if (status != PrinterStatus.PAUSED) {
                    throw new IllegalStateException(
                            "Cannot resume: printer is " + status + " (must be PAUSED)");
                }
            }
            case PRINT_CANCEL -> {
                if (status != PrinterStatus.PRINTING && status != PrinterStatus.PAUSED) {
                    throw new IllegalStateException(
                            "Cannot cancel: printer is " + status + " (must be PRINTING or PAUSED)");
                }
            }
            default -> { /* GCODE, EMERGENCY_STOP, FIRMWARE_RESTART, MACHINE_REBOOT — no status check */ }
        }
    }

    /**
     * Blocks dangerous G-code commands for regular users.
     * Extracts the first word of the G-code string and checks against the blocklist.
     */
    private void validateGcode(String payload, User.Role role) {
        if (payload == null || payload.isBlank()) {
            return;
        }

        // The G-code command is the first word (e.g. "M104 S200" → "M104")
        String command = payload.trim().split("\\s+")[0].toUpperCase();

        if (DANGEROUS_GCODES.contains(command) && role == User.Role.USER) {
            throw new SecurityException(
                    "Command '" + command + "' is restricted to ADMIN and SUPER_ADMIN users");
        }
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