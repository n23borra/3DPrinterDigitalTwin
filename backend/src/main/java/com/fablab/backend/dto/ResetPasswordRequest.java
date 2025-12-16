package com.fablab.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload for completing a password reset with the verification code.
 * Lombok provides getters and setters for each field.
 */
@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank
    @Email
    /** Account email that initiated the reset process. */
    private String email;

    @NotBlank
    /** One-time verification code sent to the user. */
    private String code;

    @NotBlank
    @Size(min = 6, max = 100)
    /** Replacement password selected by the user. */
    private String newPassword;
}