package com.fablab.backend.controllers.printer;

import com.fablab.backend.dto.PrinterCommandRequest;
import com.fablab.backend.dto.CreatePrinterRequest;
import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.models.printer.PrinterSnapshot;
import com.fablab.backend.services.printer.PrinterService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/printers")
@RequiredArgsConstructor
public class PrinterController {

    private final PrinterService printerService;

    @GetMapping
    public ResponseEntity<List<Printer>> listPrinters() {
        return ResponseEntity.ok(printerService.listPrinters());
    }

    @PostMapping
    public ResponseEntity<Printer> createPrinter(@Validated @RequestBody CreatePrinterRequest request) {
        Printer printer = printerService.createPrinter(
                request.getName(),
                request.getType(),
                request.getIpAddress(),
                request.getPort(),
                request.getApiKey()
        );
        return ResponseEntity.ok(printer);
    }

    @GetMapping("/{id}/state")
    public ResponseEntity<PrinterSnapshot> getCurrentState(@PathVariable UUID id) {
        return ResponseEntity.ok(printerService.fetchAndPersistSnapshot(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<PrinterSnapshot>> getHistory(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return ResponseEntity.ok(printerService.getHistory(id, from, to));
    }

    @PostMapping("/{id}/command")
    public ResponseEntity<Void> sendCommand(@PathVariable UUID id, @Validated @RequestBody PrinterCommandRequest request) {
        printerService.sendCommand(id, request.getCommand());
        return ResponseEntity.accepted().build();
    }
}