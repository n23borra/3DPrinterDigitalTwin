package com.fablab.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMaintenanceItemRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal thresholdHours) {
}