package com.fablab.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fablab.backend.models.MaintenanceItem;

public record MaintenanceItemDTO(
        Long id,
        String name,
        String description,
        BigDecimal totalHours,
        BigDecimal thresholdHours,
        boolean thresholdReached,
        BigDecimal remainingHoursBeforeThreshold,
        BigDecimal exceededHours,
        Instant createdAt,
        Instant updatedAt) {

    public static MaintenanceItemDTO from(MaintenanceItem item) {
        BigDecimal remaining = item.getThresholdHours().subtract(item.getTotalHours());
        BigDecimal remainingHoursBeforeThreshold = remaining.max(BigDecimal.ZERO);
        BigDecimal exceededHours = item.getTotalHours().subtract(item.getThresholdHours()).max(BigDecimal.ZERO);

        return new MaintenanceItemDTO(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getTotalHours(),
                item.getThresholdHours(),
                item.isThresholdReached(),
                remainingHoursBeforeThreshold,
                exceededHours,
                item.getCreatedAt(),
                item.getUpdatedAt());
    }
}