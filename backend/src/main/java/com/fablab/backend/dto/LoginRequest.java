package com.fablab.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Credentials payload submitted when authenticating. Lombok exposes
 * {@link #getIdentifier()}, {@link #setIdentifier(String)}, {@link #getPassword()}
 * and {@link #setPassword(String)} helpers.
 */
@Getter
@Setter
public class LoginRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    /** Username or email provided by the user. */
    public String identifier;

    @NotBlank
    @Size(min = 6, max = 100)
    /** Raw password to authenticate the user. */
    public String password;
}