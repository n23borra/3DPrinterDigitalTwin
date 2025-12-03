package com.fablab.backend.controllers;

import com.fablab.backend.models.AssetCategory;
import com.fablab.backend.repositories.AssetCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class AssetCategoryController {

    private final AssetCategoryRepository categoryRepo;

    /**
     * Lists all asset categories available in the repository.
     *
     * @return {@link ResponseEntity} containing the list of existing {@link AssetCategory} records
     */
    @GetMapping
    public ResponseEntity<List<AssetCategory>> list() {
        return ResponseEntity.ok(categoryRepo.findAll());
    }

    /**
     * Creates a new asset category.
     *
     * @param data category payload to persist; its identifier is cleared to enforce creation
     * @return {@link ResponseEntity} containing the saved {@link AssetCategory}
     */
    @PostMapping
    public ResponseEntity<AssetCategory> create(@RequestBody AssetCategory data) {
        data.setId(null);
        return ResponseEntity.ok(categoryRepo.save(data));
    }
}