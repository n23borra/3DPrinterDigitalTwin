package com.fablab.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Projection used by super administrators to manage user accounts.
 */
@Getter
@AllArgsConstructor
public class AdminUserDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
}