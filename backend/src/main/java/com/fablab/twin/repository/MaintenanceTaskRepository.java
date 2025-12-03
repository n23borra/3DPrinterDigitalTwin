package com.fablab.twin.repository;

import com.fablab.twin.domain.model.MaintenanceTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, UUID> {
}