package com.fablab.backend.repositories;

import com.fablab.backend.models.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository providing access to {@link Asset} entities.
 */
public interface AssetRepository extends JpaRepository<Asset, Long> {
    /**
     * Lists all assets that belong to the specified analysis.
     *
     * @param analysisId identifier of the parent analysis
     * @return assets associated with that analysis
     */
    List<Asset> findAllByAnalysis_Id(Long analysisId);
}
