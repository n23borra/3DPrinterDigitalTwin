package com.fablab.backend.controllers.printer;

import com.fablab.backend.dto.PrinterCommandRequest;
import com.fablab.backend.models.User;
import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.models.printer.PrinterSnapshot;
import com.fablab.backend.services.printer.PrinterService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    public ResponseEntity<Void> sendCommand( @PathVariable UUID id,
            @Validated @RequestBody PrinterCommandRequest request,
            Authentication authentication) {
        User.Role role = extractRole(authentication);
        printerService.sendCommand(id, request.getType(), request.getCommand(), role);
        return ResponseEntity.accepted().build();
    }

    /**
     * Extracts the User.Role from the Spring Security Authentication.
     * The authorities are stored as "ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN".
     */
    private User.Role extractRole(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String auth = authority.getAuthority().replace("ROLE_", "");
            try {
                return User.Role.valueOf(auth);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return User.Role.USER; // default to least privilege
    }
}