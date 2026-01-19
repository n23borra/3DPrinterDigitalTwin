package com.fablab.backend.repositories;

import com.fablab.backend.models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository exposing {@link AuditLog} entries for reporting.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    /**
     * Finds all log entries produced by the specified user.
     *
     * @param userId identifier of the user whose actions are queried
     * @return ordered list of matching audit events
     */
    List<AuditLog> findAllByUserId(Long userId);

    /**
     * Deletes all log entries produced by the specified user.
     *
     * @param userId identifier of the user whose actions are deleted
     */
    void deleteByUserId(Long userId);
}
