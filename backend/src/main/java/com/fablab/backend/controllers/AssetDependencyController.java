package com.fablab.backend.controllers;

import com.fablab.backend.dto.AssetDependencyRequest;
import com.fablab.backend.models.Asset;
import com.fablab.backend.models.AssetDependency;
import com.fablab.backend.models.User;
import com.fablab.backend.repositories.AssetDependencyRepository;
import com.fablab.backend.repositories.AssetRepository;
import com.fablab.backend.repositories.CategoryAdminRepository;
import com.fablab.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-deps")
@RequiredArgsConstructor
public class AssetDependencyController {

    private final AssetDependencyRepository repo;
    private final AssetRepository assetRepo;
    private final CategoryAdminRepository categoryAdminRepo;
    private final UserRepository userRepo;

    /**
     * Creates a dependency between two assets after validating category permissions.
     *
     * @param req request describing the parent and child asset identifiers
     * @param authentication principal used to validate administrative permissions on the parent asset category
     * @return {@link ResponseEntity} containing the persisted {@link AssetDependency}, {@code 400 Bad Request} if the parent asset is unknown,
     *         or {@code 403 Forbidden} when the user lacks rights on the category
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<AssetDependency> add(@RequestBody AssetDependencyRequest req, Authentication authentication) {
        var user = userRepo.findByUsername(authentication.getName()).orElseThrow();
        if (user.getRole() != User.Role.SUPER_ADMIN) {
            Asset parent = assetRepo.findById(req.parentId()).orElse(null);
            if (parent == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Short catId = parent.getCategory().getId();
            boolean allowed = categoryAdminRepo.existsByCategoryIdAndUserId(catId, user.getId());
            if (!allowed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        AssetDependency dep = new AssetDependency(req.parentId(), req.childId());
        return ResponseEntity.ok(repo.save(dep));
    }

    /**
     * Lists all dependencies originating from the specified asset.
     *
     * @param assetId identifier of the parent asset
     * @return list of {@link AssetDependency} records referencing the parent
     */
    @GetMapping
    public List<AssetDependency> list(@RequestParam Long assetId) {
        return repo.findAllByParentAsset(assetId);
    }

    /**
     * Removes a dependency between two assets when the caller has the required permissions.
     *
     * @param parentId identifier of the parent asset
     * @param childId identifier of the child asset
     * @param authentication principal used to validate category permissions
     * @return {@link ResponseEntity} with {@code 200 OK} once deleted, {@code 400 Bad Request} if the parent is unknown,
     *         or {@code 403 Forbidden} when the user is not authorized
     */
    @DeleteMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<?> remove(@RequestParam Long parentId, @RequestParam Long childId, Authentication authentication) {
        var user = userRepo.findByUsername(authentication.getName()).orElseThrow();
        if (user.getRole() != User.Role.SUPER_ADMIN) {
            Asset parent = assetRepo.findById(parentId).orElse(null);
            if (parent == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Short catId = parent.getCategory().getId();
            boolean allowed = categoryAdminRepo.existsByCategoryIdAndUserId(catId, user.getId());
            if (!allowed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        repo.deleteByParentAssetAndChildAsset(parentId, childId);
        return ResponseEntity.ok().build();
    }
}