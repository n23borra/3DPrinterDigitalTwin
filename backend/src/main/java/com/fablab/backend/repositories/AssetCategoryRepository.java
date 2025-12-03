package com.fablab.backend.repositories;

import com.fablab.backend.models.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository exposing {@link AssetCategory} lookup operations.
 */
public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Short> {
}

