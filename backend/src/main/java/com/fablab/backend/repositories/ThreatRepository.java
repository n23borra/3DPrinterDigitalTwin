package com.fablab.backend.repositories;

import com.fablab.backend.models.Threat;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for retrieving {@link Threat} reference data.
 */
public interface ThreatRepository             extends JpaRepository<Threat, Long> {}

