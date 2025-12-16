package com.fablab.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for initiating a password reset email. Lombok generates
 * {@link #getEmail()} and {@link #setEmail(String)} accessors.
 */
@Getter
@Setter
public class ForgotPasswordRequest {
    @NotBlank
    @Email
    /** Email address that will receive the reset instructions. */
    private String email;
}