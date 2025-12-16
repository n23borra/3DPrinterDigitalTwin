package com.fablab.backend.printer.connector;

import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.models.printer.PrinterType;

/**
 * Contract for printer integrations so that connectors can be swapped per printer type.
 */
public interface PrinterConnector {

    PrinterType getType();

    RawPrinterState fetchState(Printer printer);

    void sendCommand(Printer printer, String gcodeOrAction);
}