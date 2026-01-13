package com.fablab.backend.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Payload for updating a user's role from the super admin console.
 */
@Getter
@Setter
public class AdminUserRoleUpdateRequest {
    private String role;
}