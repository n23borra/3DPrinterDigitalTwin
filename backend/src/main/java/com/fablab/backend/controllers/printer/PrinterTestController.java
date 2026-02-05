package com.fablab.backend.controllers.printer;

import com.fablab.backend.dto.PrinterCommandType;
import com.fablab.backend.printer.connector.ConnectorRegistry;
import com.fablab.backend.printer.connector.PrinterConnector;
import com.fablab.backend.printer.connector.RawPrinterState;
import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.repositories.printer.PrinterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Test controller for printer connectivity (NO AUTHENTICATION - FOR TESTING ONLY)
 */
@RestController
@RequestMapping("/api/test/printers")
@CrossOrigin(origins = "*")
public class PrinterTestController {

    @Autowired
    private PrinterRepository printerRepository;

    @Autowired
    private ConnectorRegistry connectorRegistry;

    /**
     * Test endpoint to fetch raw printer state without authentication
     * DELETE THIS CONTROLLER IN PRODUCTION!
     */
    @GetMapping("/{id}/fetch")
    public ResponseEntity<?> testFetch(@PathVariable UUID id) {
        try {
            Printer printer = printerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Printer not found"));
            
            PrinterConnector connector = connectorRegistry.resolve(printer.getType());
            RawPrinterState state = connector.fetchState(printer);
            
            return ResponseEntity.ok(state);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * List all printers (for testing)
     */
    @GetMapping
    public ResponseEntity<?> listPrinters() {
        return ResponseEntity.ok(printerRepository.findAll());
    }

    /**
     * Send a command to a printer (for testing, no auth).
     *
     * Usage examples:
     *   POST /api/test/printers/{id}/command?type=GCODE&payload=G28
     *   POST /api/test/printers/{id}/command?type=EMERGENCY_STOP
     *   POST /api/test/printers/{id}/command?type=PRINT_PAUSE
     */
    @PostMapping("/{id}/command")
    public ResponseEntity<?> testCommand(
            @PathVariable UUID id,
            @RequestParam PrinterCommandType type,
            @RequestParam(required = false) String payload) {
        try {
            Printer printer = printerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Printer not found"));

            PrinterConnector connector = connectorRegistry.resolve(printer.getType());
            connector.sendCommand(printer, type, payload);

            return ResponseEntity.ok("Command sent: " + type + (payload != null ? " [" + payload + "]" : ""));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}