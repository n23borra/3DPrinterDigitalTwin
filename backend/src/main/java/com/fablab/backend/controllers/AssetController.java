package com.fablab.backend.controllers;


import com.fablab.backend.dto.AssetDTO;
import com.fablab.backend.dto.AssetRequest;
import com.fablab.backend.models.Asset;
import com.fablab.backend.repositories.AnalysisRepository;
import com.fablab.backend.repositories.AssetCategoryRepository;
import com.fablab.backend.repositories.AssetRepository;
import com.fablab.backend.repositories.CategoryAdminRepository;
import com.fablab.backend.repositories.UserRepository;
import com.fablab.backend.models.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AnalysisRepository analysisRepo;
    private final AssetCategoryRepository catRepo;
    private final AssetRepository assetRepo;
    private final CategoryAdminRepository categoryAdminRepo;
    private final UserRepository userRepo;

    /**
     * Returns assets, optionally filtered by analysis identifier.
     *
     * @param analysisId optional analysis identifier used to filter the assets; when absent all assets are returned
     * @return {@link ResponseEntity} containing the list of {@link AssetDTO} entries matching the filter
     */
    @GetMapping
    public ResponseEntity<List<AssetDTO>> list(@RequestParam(required = false) Long analysisId) {
        List<Asset> assets = analysisId != null
                ? assetRepo.findAllByAnalysis_Id(analysisId)
                : assetRepo.findAll();
        List<AssetDTO> dtos = assets.stream()
                .map(AssetDTO::from)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Creates a new asset and associates it with the provided analysis and category.
     *
     * @param req validated request describing the asset to create
     * @param authentication principal used to verify that the user can manage the asset's category
     * @return {@link ResponseEntity} containing the created asset, {@code 403 Forbidden} when the user is not allowed,
     *         or {@code 500 Internal Server Error} if the persistence layer fails
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<AssetDTO> add(@RequestBody @Valid AssetRequest req, Authentication authentication) {
        // Entry log
        System.out.println("[AssetController] Attempting to create asset with payload: " + req);

        try {
            var user = userRepo.findByUsername(authentication.getName()).orElseThrow();
            if (user.getRole() != User.Role.SUPER_ADMIN) {
                boolean allowed = categoryAdminRepo.existsByCategoryIdAndUserId(req.categoryId(), user.getId());
                if (!allowed) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            Asset asset = new Asset();
            asset.setAnalysis(analysisRepo.getReferenceById(req.analysisId()));
            asset.setCategory(catRepo.getReferenceById(req.categoryId()));
            asset.setName(req.name());
            asset.setDescription(req.description());
            asset.setImpactC(req.impactC());
            asset.setImpactI(req.impactI());
            asset.setImpactA(req.impactA());

            Asset saved = assetRepo.save(asset);
            System.out.println("[AssetController] Asset created successfully, id=" + saved.getId());
            return ResponseEntity.ok(AssetDTO.from(saved));
        } catch (Exception e) {
            System.err.println("[AssetController] Error while creating the asset: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Updates an existing asset.
     *
     * @param id identifier of the asset to update
     * @param req validated payload carrying the new asset attributes
     * @param authentication principal used to verify category permissions
     * @return {@link ResponseEntity} containing the updated asset, {@code 404 Not Found} if the asset does not exist,
     *         {@code 403 Forbidden} when the user lacks permissions, or {@code 500 Internal Server Error} upon persistence failures
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<AssetDTO> update(@PathVariable Long id, @RequestBody @Valid AssetRequest req,
                                           Authentication authentication) {
        var assetOpt = assetRepo.findById(id);
        if (assetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            var user = userRepo.findByUsername(authentication.getName()).orElseThrow();
            if (user.getRole() != User.Role.SUPER_ADMIN) {
                boolean allowed = categoryAdminRepo.existsByCategoryIdAndUserId(req.categoryId(), user.getId());
                if (!allowed) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            Asset asset = assetOpt.get();
            asset.setAnalysis(analysisRepo.getReferenceById(req.analysisId()));
            asset.setCategory(catRepo.getReferenceById(req.categoryId()));
            asset.setName(req.name());
            asset.setDescription(req.description());
            asset.setImpactC(req.impactC());
            asset.setImpactI(req.impactI());
            asset.setImpactA(req.impactA());
            Asset saved = assetRepo.save(asset);
            return ResponseEntity.ok(AssetDTO.from(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deletes an asset when the requesting user is authorized to manage its category.
     *
     * @param id identifier of the asset to delete
     * @param authentication principal used to verify category permissions
     * @return {@link ResponseEntity} with {@code 200 OK} when removed, {@code 404 Not Found} if the asset is missing,
     *         or {@code 403 Forbidden} when the user lacks permissions
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        var assetOpt = assetRepo.findById(id);
        if (assetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var user = userRepo.findByUsername(authentication.getName()).orElseThrow();
        if (user.getRole() != User.Role.SUPER_ADMIN) {
            Short catId = assetOpt.get().getCategory().getId();
            boolean allowed = categoryAdminRepo.existsByCategoryIdAndUserId(catId, user.getId());
            if (!allowed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        assetRepo.delete(assetOpt.get());
        return ResponseEntity.ok().build();
    }
}

