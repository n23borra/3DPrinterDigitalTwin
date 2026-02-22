package com.fablab.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload for issuing commands to a printer.
 *
 * Examples:
 *   {"type": "GCODE", "command": "G28"}          → home all axes
 *   {"type": "EMERGENCY_STOP"}                    → immediate stop
 *   {"type": "PRINT_PAUSE"}                       → pause current print
 */
@Getter
@Setter
public class PrinterCommandRequest {

    /** The kind of command to execute (required). */
    @NotNull
    private PrinterCommandType type;

    /**
     * The G-code string to send (only used when type = GCODE).
     * Ignored for action commands like EMERGENCY_STOP, PRINT_PAUSE, etc.
     */
    private String command;
}