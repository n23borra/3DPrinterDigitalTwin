package com.fablab.backend.models.printer;

/**
 * High-level operational status for a printer derived from its latest snapshot.
 */
public enum PrinterStatus {
    IDLE,
    PRINTING,
    PAUSED,
    OFFLINE
}