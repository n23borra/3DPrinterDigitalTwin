package com.fablab.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response wrapper returning the JWT issued after authentication. Lombok
 * exposes {@link #getToken()} for serialization.
 */
@Getter
@AllArgsConstructor
public class TokenResponse {
    /** JWT string provided to the client. */
    private String token;
}