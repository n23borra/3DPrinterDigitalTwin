package com.fablab.twin.service.dto;

import java.time.Instant;

public record RawPrinterState(
        Double bedTemp,
        Double nozzleTemp,
        Double targetBed,
        Double targetNozzle,
        Double progress,
        Integer layer,
        Double zHeight,
        String state,
        String rawPayload,
        Instant timestamp
) {}