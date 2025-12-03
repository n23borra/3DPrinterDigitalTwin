package com.fablab.backend.services;

import com.fablab.backend.dto.CategoryAdminAssignmentDTO;
import com.fablab.backend.models.CategoryAdmin;
import com.fablab.backend.models.User;
import com.fablab.backend.repositories.AssetCategoryRepository;
import com.fablab.backend.repositories.CategoryAdminRepository;
import com.fablab.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryAdminService {

    private final AssetCategoryRepository categoryRepo;
    private final CategoryAdminRepository categoryAdminRepo;
    private final UserRepository userRepo;

    private static final Logger log = LoggerFactory.getLogger(CategoryAdminService.class);

    /**
     * Saves the provided category administrator assignments, replacing any existing mappings for the affected categories.
     * @param assignments list of assignments pairing category identifiers with administrator emails
     * @throws IllegalArgumentException if an unknown category identifier is supplied
     */
    @Transactional
    public void saveAll(List<CategoryAdminAssignmentDTO> assignments) {
        Set<Long> affectedUsers = new HashSet<>();
        for (CategoryAdminAssignmentDTO dto : assignments) {
            Short categoryId = dto.categoryId();
            if (!categoryRepo.existsById(categoryId)) {
                log.warn("Unknown category {}", categoryId);
                throw new IllegalArgumentException("Unknown category ID " + categoryId);
            }
            var previous = categoryAdminRepo.findAllByCategoryId(categoryId);
            Set<Long> previousIds = previous.stream()
                    .map(CategoryAdmin::getUserId)
                    .collect(Collectors.toSet());

            categoryAdminRepo.deleteByCategoryId(categoryId);

            Set<Long> assignedIds = new HashSet<>();
            if (dto.emails() != null) {
                for (String email : dto.emails()) {
                    userRepo.findByEmail(email).ifPresentOrElse(user -> {
                        if (user.getRole() == User.Role.USER) {
                            user.setRole(User.Role.ADMIN);
                            userRepo.save(user);
                        }
                        categoryAdminRepo.save(new CategoryAdmin(categoryId, user.getId()));
                        assignedIds.add(user.getId());
                    }, () -> log.warn("Ignoring unknown admin email {}", email));
                }
            }

            affectedUsers.addAll(previousIds);
            affectedUsers.addAll(assignedIds);
        }

        for (Long userId : affectedUsers) {
            if (categoryAdminRepo.findAllByUserId(userId).isEmpty()) {
                userRepo.findById(userId).ifPresent(user -> {
                    if (user.getRole() == User.Role.ADMIN) {
                        user.setRole(User.Role.USER);
                        userRepo.save(user);
                    }
                });
            }
        }
    }
}