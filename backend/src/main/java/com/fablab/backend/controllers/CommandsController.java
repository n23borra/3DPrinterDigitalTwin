package com.fablab.backend.controllers;

import com.fablab.backend.commands.CommandsService;
import com.fablab.backend.commands.dto.AvailableCommandDTO;
import com.fablab.backend.commands.dto.CommandExecuteRequest;
import com.fablab.backend.commands.dto.CommandExecuteResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/commands")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class CommandsController {

    private final CommandsService commandsService;

    @GetMapping("/available")
    public ResponseEntity<List<AvailableCommandDTO>> getAvailable(@RequestParam UUID printerId) {
        return ResponseEntity.ok(commandsService.getAvailableCommands(printerId));
    }

    @PostMapping("/execute")
    public ResponseEntity<CommandExecuteResponse> execute(@Valid @RequestBody CommandExecuteRequest request,
                                                          Authentication authentication) {
        Instant executedAt = commandsService.executeCommand(request.printerId(), request.commandKey(), authentication);
        return ResponseEntity.ok(new CommandExecuteResponse(true, "Command executed", executedAt));
    }
}