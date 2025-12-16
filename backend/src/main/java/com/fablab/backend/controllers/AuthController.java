package com.fablab.backend.controllers;

import com.fablab.backend.dto.LoginRequest;
import com.fablab.backend.dto.PasswordUpdateRequest;
import com.fablab.backend.dto.TokenResponse;
import com.fablab.backend.dto.ForgotPasswordRequest;
import com.fablab.backend.dto.ResetPasswordRequest;
import com.fablab.backend.services.AuthService;
import com.fablab.backend.services.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.fablab.backend.dto.RegisterRequest;


//@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
//@RequestMapping("/auth")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    /**
     * Authenticates a user with the provided identifier and password and issues a JWT on success.
     *
     * @param request login credentials containing either the username or email and the password
     * @return a {@link TokenResponse} wrapping the generated JWT when authentication succeeds
     */
    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Registers a new user account.
     *
     * @param request validated registration payload containing username, email and password
     * @return {@link ResponseEntity} with the confirmation message when the account is created,
     *         or {@code 400 Bad Request} if the username or email already exists
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Updates the authenticated user's password after verifying the current password.
     *
     * @param request payload containing the current and new password values
     * @param authentication Spring security context holding the authenticated principal
     * @return {@link ResponseEntity} with a success message when the update completes, or
     *         {@code 400 Bad Request} if the current password is incorrect or the user is missing
     */
    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody @Valid PasswordUpdateRequest request, Authentication authentication) {
        try {
            return ResponseEntity.ok(authService.updatePassword(authentication.getName(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Initiates the password reset process by generating and emailing a reset code.
     *
     * @param request payload containing the email that should receive the reset code
     * @return {@link ResponseEntity} confirming the code dispatch, or {@code 400 Bad Request} if the email is unknown
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        try {
            passwordResetService.createToken(request.getEmail());
            return ResponseEntity.ok("Reset code sent");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Completes the password reset using a previously generated code.
     *
     * @param request payload with the account email, reset code and the new password to apply
     * @return {@link ResponseEntity} acknowledging the successful reset, or {@code 400 Bad Request} for invalid or expired codes
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

