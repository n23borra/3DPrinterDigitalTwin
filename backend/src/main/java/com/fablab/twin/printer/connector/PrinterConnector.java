package com.fablab.twin.printer.connector;

import com.fablab.twin.domain.model.Printer;
import com.fablab.twin.service.dto.RawPrinterState;

public interface PrinterConnector {
    boolean supports(Printer printer);

    RawPrinterState fetchState(Printer printer);

    void sendCommand(Printer printer, String gcodeOrAction);
}