package com.fablab.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AddMaintenanceHoursRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal hoursToAdd) {
}