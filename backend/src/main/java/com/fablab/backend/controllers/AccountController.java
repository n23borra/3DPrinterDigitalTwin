package com.fablab.backend.controllers;

import com.fablab.backend.dto.PasswordUpdateRequest;
import com.fablab.backend.models.User;
import com.fablab.backend.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Updates the password of the currently authenticated user after verifying the current password.
     *
     * @param request validated payload containing the current password and the desired new password
     * @param authentication security context used to resolve the logged-in username
     * @return {@link ResponseEntity} with a confirmation message when the password is changed,
     *         or {@code 400 Bad Request} if the user cannot be found or the current password is invalid
     */
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid PasswordUpdateRequest request, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body("Current password is incorrect.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Password updated successfully.");
    }
}
