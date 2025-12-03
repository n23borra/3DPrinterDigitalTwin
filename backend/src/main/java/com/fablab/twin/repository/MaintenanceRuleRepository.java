package com.fablab.twin.repository;

import com.fablab.twin.domain.model.MaintenanceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MaintenanceRuleRepository extends JpaRepository<MaintenanceRule, UUID> {
}