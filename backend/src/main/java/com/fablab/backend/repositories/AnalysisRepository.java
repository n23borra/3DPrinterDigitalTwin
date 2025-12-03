package com.fablab.backend.repositories;

import com.fablab.backend.models.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository providing CRUD access to {@link Analysis} entities.
 */
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    /**
     * Retrieves all analyses created by the specified user.
     *
     * @param ownerId identifier of the owning user
     * @return analyses belonging to that user
     */
    List<Analysis> findAllByOwnerId(Long ownerId);
}
