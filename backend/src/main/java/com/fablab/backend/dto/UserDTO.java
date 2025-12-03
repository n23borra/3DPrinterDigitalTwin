package com.fablab.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Lightweight projection of an authenticated user. Lombok supplies getters for
 * serialization.
 */
@Getter
@AllArgsConstructor
public class UserDTO {
    /** Username presented to the client. */
    private String username;
    /** Role granted to the user (e.g., ADMIN, USER). */
    private String role;
}
