package com.fablab.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fablab.backend.models.Alert;

/**
 * Repository for Alert entities.
 */
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    /**
     * Finds all alerts for a specific user.
     *
     * @param userId id of the user
     * @return list of alerts owned by the user
     */
    List<Alert> findAllByUserId(Long userId);
    
    /**
     * Finds all unresolved alerts for a user.
     *
     * @param userId id of the user
     * @param resolved false to get unresolved alerts
     * @return list of unresolved alerts
     */
    List<Alert> findByUserIdAndStatus(Long userId, Alert.Status status);
    
    /**
     * Finds all unresolved alerts globally (for admins).
     *
     * @param resolved false to get unresolved alerts
     * @return list of unresolved alerts
     */
    List<Alert> findByStatus(Alert.Status status);

    List<Alert> findByPrinterId(UUID printerId);
}
