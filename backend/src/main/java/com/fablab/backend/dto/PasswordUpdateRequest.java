package com.fablab.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload used by authenticated users to change their password. Lombok exposes
 * {@link #getCurrentPassword()}, {@link #setCurrentPassword(String)},
 * {@link #getNewPassword()} and {@link #setNewPassword(String)}.
 */
@Getter
@Setter
public class PasswordUpdateRequest {
    @NotBlank
    /** Existing password used to verify the request. */
    private String currentPassword;

    @NotBlank
    @Size(min = 6, max = 100)
    /** New password that replaces the current credential. */
    private String newPassword;
}