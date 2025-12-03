package com.fablab.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload submitted when creating a new account. Lombok generates the
 * standard getters and setters for each field.
 */
@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    /** Preferred username for the account. */
    private String username;

    @NotBlank
    @Email
    /** Contact email that also acts as an alternate login identifier. */
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    /** Initial password chosen by the user. */
    private String password;
}