package com.fablab.backend.controllers;

import com.fablab.backend.dto.AdminUserDTO;
import com.fablab.backend.dto.AdminUserRoleUpdateRequest;
import com.fablab.backend.services.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * Lists users based on optional search and role filters.
     *
     * @param search optional search string (username or email)
     * @param role   optional role filter
     * @return list of users for administration
     */
    @GetMapping
    public List<AdminUserDTO> listUsers(@RequestParam(required = false) String search,
                                        @RequestParam(required = false) String role) {
        return adminUserService.listUsers(search, role);
    }

    /**
     * Updates the role of a user.
     *
     * @param id            user id
     * @param request       role update payload
     * @param authentication authenticated principal
     * @return updated user projection
     */
    @PatchMapping("/{id}/role")
    public AdminUserDTO updateUserRole(@PathVariable Long id,
                                       @RequestBody AdminUserRoleUpdateRequest request,
                                       Authentication authentication) {
        return adminUserService.changeRole(id, request.getRole(), authentication.getName());
    }

    /**
     * Deletes the specified user account.
     *
     * @param id            user id
     * @param authentication authenticated principal
     * @return no content response when deleted
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        adminUserService.deleteUser(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}