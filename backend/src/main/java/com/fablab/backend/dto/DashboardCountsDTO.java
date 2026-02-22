package com.fablab.backend.dto;

/**
 * Aggregated counters displayed in the dashboard widgets.
 */
public record DashboardCountsDTO(long userCount, long alertCount, long printerAvailableCount) {
}