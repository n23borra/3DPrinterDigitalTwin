package com.fablab.backend.printer.connector;

import com.fablab.backend.models.printer.PrinterType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry used to resolve the appropriate connector from the printer type.
 */
@Component
public class ConnectorRegistry {

    private final Map<PrinterType, PrinterConnector> connectors = new EnumMap<>(PrinterType.class);

    public ConnectorRegistry(List<PrinterConnector> connectors) {
        connectors.forEach(connector -> this.connectors.put(connector.getType(), connector));
    }

    public PrinterConnector resolve(PrinterType type) {
        PrinterConnector connector = connectors.get(type);
        if (connector == null) {
            throw new IllegalArgumentException("No connector registered for type " + type);
        }
        return connector;
    }
}