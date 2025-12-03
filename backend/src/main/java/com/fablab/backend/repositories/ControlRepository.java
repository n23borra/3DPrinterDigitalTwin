package com.fablab.backend.repositories;

import com.fablab.backend.models.Control;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository accessing {@link Control} definitions.
 */
public interface ControlRepository            extends JpaRepository<Control, Long> {}

