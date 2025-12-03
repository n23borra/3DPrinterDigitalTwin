package com.fablab.backend.controllers;

import com.fablab.backend.dto.CategoryAdminAssignmentDTO;
import com.fablab.backend.models.AssetCategory;
import com.fablab.backend.models.User;
import com.fablab.backend.repositories.AssetCategoryRepository;
import com.fablab.backend.repositories.CategoryAdminRepository;
import com.fablab.backend.repositories.UserRepository;
import com.fablab.backend.services.CategoryAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({CategoryAdminController.class, CategoryAdminService.class})
class CategoryAdminControllerTest {

    @Autowired
    private CategoryAdminController controller;

    @Autowired
    private AssetCategoryRepository categoryRepo;

    @Autowired
    private CategoryAdminRepository categoryAdminRepo;

    @Autowired
    private UserRepository userRepo;

    @Test
    void removingOneOfMultipleCategoriesKeepsAdminRole() {
        // create user and categories
        User user = User.builder()
                .username("user")
                .email("user@test.com")
                .passwordHash("pass")
                .role(User.Role.USER)
                .build();
        userRepo.save(user);

        AssetCategory cat1 = new AssetCategory();
        cat1.setLabel("cat1");
        categoryRepo.save(cat1);

        AssetCategory cat2 = new AssetCategory();
        cat2.setLabel("cat2");
        categoryRepo.save(cat2);

        // assign user as admin for both categories
        controller.assign(List.of(
                new CategoryAdminAssignmentDTO(cat1.getId(), cat1.getLabel(), List.of(user.getEmail())),
                new CategoryAdminAssignmentDTO(cat2.getId(), cat2.getLabel(), List.of(user.getEmail()))
        ), null);

        assertEquals(User.Role.ADMIN, userRepo.findById(user.getId()).orElseThrow().getRole());
        assertTrue(categoryAdminRepo.existsByCategoryIdAndUserId(cat1.getId(), user.getId()));
        assertTrue(categoryAdminRepo.existsByCategoryIdAndUserId(cat2.getId(), user.getId()));

        // remove user from category 1 only
        controller.assign(List.of(
                new CategoryAdminAssignmentDTO(cat1.getId(), cat1.getLabel(), List.of())
        ), null);

        User updated = userRepo.findById(user.getId()).orElseThrow();
        assertEquals(User.Role.ADMIN, updated.getRole());
        assertFalse(categoryAdminRepo.existsByCategoryIdAndUserId(cat1.getId(), user.getId()));
        assertTrue(categoryAdminRepo.existsByCategoryIdAndUserId(cat2.getId(), user.getId()));
    }


    @Test
    void assigningUnknownCategoryReturnsBadRequest() {
        User user = User.builder()
                .username("user2")
                .email("user2@test.com")
                .passwordHash("pass")
                .role(User.Role.USER)
                .build();
        userRepo.save(user);

        var response = controller.assign(List.of(
                new CategoryAdminAssignmentDTO((short) 9999, "unknown", List.of(user.getEmail()))
        ), null);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals(User.Role.USER, userRepo.findById(user.getId()).orElseThrow().getRole());
        assertTrue(categoryAdminRepo.findAll().isEmpty());
    }


    @Test
    void removingLastCategoryRevokesAdminRole() {
        // create user and category
        User user = User.builder()
                .username("solo")
                .email("solo@test.com")
                .passwordHash("pass")
                .role(User.Role.USER)
                .build();
        userRepo.save(user);

        AssetCategory cat = new AssetCategory();
        cat.setLabel("only");
        categoryRepo.save(cat);

        // assign user as admin for the single category
        controller.assign(List.of(
                new CategoryAdminAssignmentDTO(cat.getId(), cat.getLabel(), List.of(user.getEmail()))
        ), null);

        assertEquals(User.Role.ADMIN, userRepo.findById(user.getId()).orElseThrow().getRole());
        assertTrue(categoryAdminRepo.existsByCategoryIdAndUserId(cat.getId(), user.getId()));

        // remove all admins from the category
        controller.assign(List.of(
                new CategoryAdminAssignmentDTO(cat.getId(), cat.getLabel(), List.of())
        ), null);

        User updated = userRepo.findById(user.getId()).orElseThrow();
        assertEquals(User.Role.USER, updated.getRole());
        assertTrue(categoryAdminRepo.findAll().isEmpty());
    }
}