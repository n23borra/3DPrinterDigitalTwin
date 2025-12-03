package com.fablab.backend.repositories;

import com.fablab.backend.models.ControlImplementation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository accessing {@link ControlImplementation} join records.
 */
public interface ControlImplementationRepository extends JpaRepository<ControlImplementation, ControlImplementation.PK> {

    /**
     * Lists control implementations associated with the provided risk.
     *
     * @param riskId identifier of the risk
     * @return control implementations for that risk
     */
    List<ControlImplementation> findByRisk_Id(Long riskId);
}
