package com.fablab.backend.printer.connector;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Raw state returned by a connector before persistence or normalization.
 */
@Value
@Builder
public class RawPrinterState {
    Double bedTemp;
    Double nozzleTemp;
    Double targetBed;
    Double targetNozzle;
    Double progress;
    Double zHeight;
    String state;
    String rawPayload;
    Instant timestamp;
}