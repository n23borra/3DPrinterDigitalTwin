package com.fablab.backend.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fablab.backend.dto.CreateMaintenanceItemRequest;
import com.fablab.backend.models.MaintenanceItem;
import com.fablab.backend.repositories.MaintenanceItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceItemRepository maintenanceItemRepository;

    public List<MaintenanceItem> listAll() {
        return maintenanceItemRepository.findAll();
    }

    public MaintenanceItem getById(Long id) {
        return maintenanceItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance item not found: " + id));
    }

    @Transactional
    public MaintenanceItem create(CreateMaintenanceItemRequest request) {
        MaintenanceItem item = MaintenanceItem.builder()
                .name(request.name().trim())
                .description(request.description())
                .thresholdHours(request.thresholdHours())
                .totalHours(BigDecimal.ZERO)
                .thresholdReached(false)
                .build();
        return maintenanceItemRepository.save(item);
    }

    @Transactional
    public MaintenanceItem addHours(Long id, BigDecimal hoursToAdd) {
        MaintenanceItem item = getById(id);
        item.setTotalHours(item.getTotalHours().add(hoursToAdd));
        item.setThresholdReached(item.getTotalHours().compareTo(item.getThresholdHours()) >= 0);
        return maintenanceItemRepository.save(item);
    }
}