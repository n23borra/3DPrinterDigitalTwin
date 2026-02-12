package com.fablab.backend.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fablab.backend.commands.dto.AvailableCommandDTO;
import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.repositories.UserRepository;
import com.fablab.backend.services.AuditLogService;
import com.fablab.backend.services.printer.PrinterService;
import com.fablab.backend.printer.connector.MoonrakerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommandsService {

    private final PrinterService printerService;
    private final CommandCatalog commandCatalog;
    private final MoonrakerClient moonrakerClient;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    public List<AvailableCommandDTO> getAvailableCommands(java.util.UUID printerId) {
        Printer printer = printerService.getPrinter(printerId);
        Set<String> availableMacros = fetchAvailableMacros(printer);

        return commandCatalog.all().stream()
                .filter(cmd -> !cmd.requiresMacro() || availableMacros.contains(cmd.macroName()))
                .map(cmd -> new AvailableCommandDTO(cmd.key(), cmd.label(), cmd.group(), cmd.dangerous()))
                .sorted((a, b) -> a.group().compareToIgnoreCase(b.group()))
                .toList();
    }

    public Instant executeCommand(java.util.UUID printerId, CommandKey commandKey, Authentication authentication) {
        Printer printer = printerService.getPrinter(printerId);
        CommandDefinition definition = commandCatalog.get(commandKey);

        if (definition == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown command key");
        }

        if (definition.requiresMacro()) {
            Set<String> macros = fetchAvailableMacros(printer);
            if (!macros.contains(definition.macroName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Required macro is not available on printer: " + definition.macroName());
            }
        }

        try {
            String payload = objectMapper.writeValueAsString(java.util.Map.of("script", definition.script()));
            moonrakerClient.postJson(buildBaseUrl(printer), printer.getApiKey(), "/printer/gcode/script", payload);

            if (authentication != null) {
                userRepository.findByUsername(authentication.getName())
                        .ifPresent(user -> auditLogService.logAction(
                                user.getId(),
                                "COMMAND_EXECUTE",
                                "printerId=" + printer.getId() + ",commandKey=" + commandKey
                        ));
            }

            return Instant.now();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to execute command on Moonraker", e);
        }
    }

    private Set<String> fetchAvailableMacros(Printer printer) {
        try {
            String response = moonrakerClient.get(buildBaseUrl(printer), printer.getApiKey(), "/printer/objects/list");
            JsonNode root = objectMapper.readTree(response);
            JsonNode list = root.path("result").path("objects");
            Set<String> macros = new HashSet<>();
            if (list.isArray()) {
                for (JsonNode node : list) {
                    String raw = node.asText();
                    if (raw.startsWith("gcode_macro ")) {
                        macros.add(raw.substring("gcode_macro ".length()).trim().toUpperCase());
                    }
                }
            }
            return macros;
        } catch (Exception e) {
            return Set.of();
        }
    }

    private String buildBaseUrl(Printer printer) {
        int port = printer.getPort() == null ? 7125 : printer.getPort();
        return "http://" + printer.getIpAddress() + ":" + port;
    }
}