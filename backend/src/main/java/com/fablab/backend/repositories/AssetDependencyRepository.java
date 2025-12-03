package com.fablab.backend.repositories;

import com.fablab.backend.models.AssetDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository managing dependencies between assets.
 */
public interface AssetDependencyRepository extends JpaRepository<AssetDependency, AssetDependency.PK> {

    /**
     * Loads every dependency where the given asset is the parent in the
     * relationship.
     *
     * @param parentAsset identifier of the upstream asset
     * @return dependencies pointing to child assets
     */
    List<AssetDependency> findAllByParentAsset(Long parentAsset);

    /**
     * Removes a dependency link between the provided parent and child.
     *
     * @param parentAsset identifier of the upstream asset
     * @param childAsset  identifier of the downstream asset
     */
    void deleteByParentAssetAndChildAsset(Long parentAsset, Long childAsset);
}
