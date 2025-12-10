package com.fablab.twin.controller;

import com.fablab.twin.domain.dto.PrinterCommandRequest;
import com.fablab.twin.domain.model.Printer;
import com.fablab.twin.domain.model.PrinterSnapshot;
import com.fablab.twin.printer.connector.PrinterConnector;
import com.fablab.twin.service.PrinterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/printers")
public class PrinterController {

    private final PrinterService printerService;
    private final List<PrinterConnector> connectors;

    public PrinterController(PrinterService printerService, List<PrinterConnector> connectors) {
        this.printerService = printerService;
        this.connectors = connectors;
    }

    @GetMapping
    public List<Printer> list() {
        return printerService.findAll();
    }

    @PostMapping
    public Printer create(@RequestBody Printer printer) {
        return printerService.save(printer);
    }

    @GetMapping("/{id}/history")
    public List<PrinterSnapshot> history(@PathVariable UUID id) {
        return printerService.findSnapshots(id);
    }

    @PostMapping("/{id}/command")
    public ResponseEntity<Void> sendCommand(@PathVariable UUID id, @Valid @RequestBody PrinterCommandRequest request) {
        Printer printer = printerService.findAll().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow();

        connectors.stream()
                .filter(c -> c.supports(printer))
                .findFirst()
                .ifPresent(connector -> connector.sendCommand(printer, request.command()));

        return ResponseEntity.accepted().build();
    }

    // --------------------------------------------------------------
    // 🔧 NEW ENDPOINT FOR TESTING MOONRAKER COMMUNICATION
    // --------------------------------------------------------------
    @GetMapping("/test-moonraker")
    public PrinterState testMoonraker() throws Exception {
        MoonrakerConfig cfg = new MoonrakerConfig();
        MoonrakerClient client = new MoonrakerClient(cfg);
        MoonrakerQueries queries = new MoonrakerQueries(client);
        PrinterState state = new PrinterState();
        PrinterDataCollector collector = new PrinterDataCollector(queries, state);

        collector.refreshAll();
        return state;
    }
}