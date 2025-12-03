package com.fablab.backend.controllers;

import com.fablab.backend.dto.CategoryAdminAssignmentDTO;
import com.fablab.backend.models.User;
import com.fablab.backend.repositories.AssetCategoryRepository;
import com.fablab.backend.repositories.CategoryAdminRepository;
import com.fablab.backend.repositories.UserRepository;
import com.fablab.backend.services.CategoryAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/category-admins")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final AssetCategoryRepository categoryRepo;
    private final CategoryAdminRepository categoryAdminRepo;
    private final UserRepository userRepo;
    private final CategoryAdminService categoryAdminService;

    private static final Logger log = LoggerFactory.getLogger(CategoryAdminController.class);

    /**
     * Retrieves all asset categories and the emails of the administrators assigned to each.
     *
     * @return {@link ResponseEntity} containing a list of {@link CategoryAdminAssignmentDTO} grouped by category
     */
    @GetMapping
    public ResponseEntity<List<CategoryAdminAssignmentDTO>> list() {
        List<CategoryAdminAssignmentDTO> result = categoryRepo.findAll().stream()
                .map(cat -> {
                    List<String> emails = categoryAdminRepo.findAllByCategoryId(cat.getId()).stream()
                            .map(ca -> userRepo.findById(ca.getUserId()).map(User::getEmail).orElse(null))
                            .filter(e -> e != null)
                            .toList();
                    return new CategoryAdminAssignmentDTO(cat.getId(), cat.getLabel(), emails);
                }).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * Persists the provided category administrator assignments.
     *
     * @param assignments list of category-to-user assignments to store
     * @param authentication principal making the request, logged for traceability
     * @return {@link ResponseEntity} with {@code 200 OK} when saved, or {@code 400 Bad Request} if an unknown category or user email is provided
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> assign(@RequestBody List<CategoryAdminAssignmentDTO> assignments, Authentication authentication) {
        if (authentication != null) {
            log.info("Assign category admins requested by {} with authorities {}", authentication.getName(), authentication.getAuthorities());
        }
        try {
            categoryAdminService.saveAll(assignments);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}