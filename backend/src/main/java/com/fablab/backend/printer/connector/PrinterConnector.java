package com.fablab.backend.printer.connector;

import com.fablab.backend.dto.PrinterCommandType;
import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.models.printer.PrinterType;

/**
 * Contract for printer integrations so that connectors can be swapped per printer type.
 */
public interface PrinterConnector {

    PrinterType getType();

    RawPrinterState fetchState(Printer printer);

    /**
     * Send a command to the printer.
     *
     * @param printer the target printer
     * @param type    the kind of command (GCODE, PRINT_PAUSE, EMERGENCY_STOP, etc.)
     * @param payload optional data — the G-code string for GCODE type, null for action types
     */
    void sendCommand(Printer printer, PrinterCommandType type, String payload);
}