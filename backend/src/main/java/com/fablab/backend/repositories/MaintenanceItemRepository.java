package com.fablab.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fablab.backend.models.MaintenanceItem;

public interface MaintenanceItemRepository extends JpaRepository<MaintenanceItem, Long> {
}