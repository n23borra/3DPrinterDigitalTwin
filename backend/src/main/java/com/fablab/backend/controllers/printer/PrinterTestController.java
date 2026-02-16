package com.fablab.backend.controllers.printer;

import com.fablab.backend.dto.CreatePrinterRequest;
import com.fablab.backend.printer.connector.ConnectorRegistry;
import com.fablab.backend.printer.connector.PrinterConnector;
import com.fablab.backend.printer.connector.RawPrinterState;
import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.repositories.printer.PrinterRepository;
import com.fablab.backend.services.printer.PrinterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

    @Autowired
    private PrinterService printerService;

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

    @PostMapping
    public ResponseEntity<?> createPrinter(@Validated @RequestBody CreatePrinterRequest request) {
        Printer printer = printerService.createPrinter(
                request.getName(),
                request.getType(),
                request.getIpAddress(),
                request.getPort(),
                request.getApiKey()
        );
        return ResponseEntity.ok(printer);
    }
}