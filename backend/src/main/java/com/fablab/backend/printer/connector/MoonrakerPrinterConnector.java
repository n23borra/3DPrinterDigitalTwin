package com.fablab.backend.printer.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.models.printer.PrinterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

/**
 * Connector implementation for Moonraker/Klipper powered printers.
 */
@Component
public class MoonrakerPrinterConnector implements PrinterConnector {

    private static final Logger log = LoggerFactory.getLogger(MoonrakerPrinterConnector.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PrinterType getType() {
        return PrinterType.MOONRAKER;
    }

    @Override
    public RawPrinterState fetchState(Printer printer) {
        try {
            URI uri = URI.create(String.format("http://%s:%d/printer/objects/query?heater_bed&extruder&print_stats&toolhead",
                    printer.getIpAddress(), printer.getPort()));
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(uri, JsonNode.class);
            JsonNode root = Optional.ofNullable(response.getBody()).orElseGet(objectMapper::createObjectNode);
            JsonNode status = root.path("result").path("status");

            Double bed = status.path("heater_bed").path("temperature").isMissingNode() ? null : status.path("heater_bed").path("temperature").asDouble();
            Double targetBed = status.path("heater_bed").path("target").isMissingNode() ? null : status.path("heater_bed").path("target").asDouble();
            Double nozzle = status.path("extruder").path("temperature").isMissingNode() ? null : status.path("extruder").path("temperature").asDouble();
            Double targetNozzle = status.path("extruder").path("target").isMissingNode() ? null : status.path("extruder").path("target").asDouble();
            Double progress = status.path("print_stats").path("progress").isMissingNode() ? null : status.path("print_stats").path("progress").asDouble() * 100;
            String state = status.path("print_stats").path("state").asText(null);
            Double zHeight = status.path("toolhead").path("position").isArray() && status.path("toolhead").path("position").size() > 2
                    ? status.path("toolhead").path("position").get(2).asDouble()
                    : null;

            return RawPrinterState.builder()
                    .bedTemp(bed)
                    .targetBed(targetBed)
                    .nozzleTemp(nozzle)
                    .targetNozzle(targetNozzle)
                    .progress(progress)
                    .state(state)
                    .zHeight(zHeight)
                    .rawPayload(root.toString())
                    .timestamp(Instant.now())
                    .build();
        } catch (Exception e) {
            log.warn("Failed to fetch state from Moonraker printer {}:{}", printer.getIpAddress(), printer.getPort(), e);
            throw new RuntimeException("Unable to reach printer", e);
        }
    }

    @Override
    public void sendCommand(Printer printer, String gcodeOrAction) {
        try {
            URI uri = URI.create(String.format("http://%s:%d/printer/gcode/script?script=%s",
                    printer.getIpAddress(), printer.getPort(), gcodeOrAction));
            restTemplate.postForEntity(uri, null, Void.class);
        } catch (Exception e) {
            log.warn("Failed to send command to Moonraker printer {}:{}", printer.getIpAddress(), printer.getPort(), e);
            throw new RuntimeException("Unable to send command", e);
        }
    }
}