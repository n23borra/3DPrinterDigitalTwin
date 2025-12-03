package com.fablab.twin.printer.connector;

import com.fablab.twin.domain.model.Printer;
import com.fablab.twin.domain.model.PrinterType;
import com.fablab.twin.service.dto.RawPrinterState;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class MoonrakerPrinterConnector implements PrinterConnector {

    private final WebClient webClient;

    public MoonrakerPrinterConnector(WebClient moonrakerWebClient) {
        this.webClient = moonrakerWebClient;
    }

    @Override
    public boolean supports(Printer printer) {
        return printer.getType() == PrinterType.CREALITY_K2;
    }

    @Override
    public RawPrinterState fetchState(Printer printer) {
        String query = "/printer/objects/query?heater_bed&extruder&print_stats&display_status";
        Mono<String> payloadMono = webClient.get()
                .uri(query)
                .retrieve()
                .bodyToMono(String.class);
        String payload = payloadMono.block();
        // In real-world scenario we would parse JSON; here we mock simple mapping for brevity
        return new RawPrinterState(null, null, null, null, null, null, null, "UNKNOWN", payload, Instant.now());
    }

    @Override
    public void sendCommand(Printer printer, String gcodeOrAction) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/printer/gcode/script")
                        .queryParam("script", gcodeOrAction)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}