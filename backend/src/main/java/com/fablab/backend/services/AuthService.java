package com.fablab.backend.services;

import com.fablab.backend.dto.LoginRequest;
import com.fablab.backend.dto.PasswordUpdateRequest;
import com.fablab.backend.dto.RegisterRequest;
import com.fablab.backend.dto.TokenResponse;
import com.fablab.backend.models.User;
import com.fablab.backend.repositories.UserRepository;
import com.fablab.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuditLogService auditService;


    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /**
     * Authenticates a user and produces a JWT token.
     *
     * @param request login payload containing the identifier (username or email) and password
     * @return {@link TokenResponse} wrapping the issued JWT for the authenticated user
     * @throws RuntimeException when authentication fails
     */
    public TokenResponse login(LoginRequest request) {
        try {
            Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword()));
            User user = userRepository.findByUsername(request.getIdentifier()).or(() -> userRepository.findByEmail(request.getIdentifier())).orElseThrow();
            auditService.logAction(user.getId(), "LOGIN_SUCCESS", null);
            return new TokenResponse(jwtProvider.generateToken(user));
        } catch (Exception e) {
            log.warn("Login failed for user: {}", request.getIdentifier());
            userRepository.findByUsername(request.getIdentifier()).or(() -> userRepository.findByEmail(request.getIdentifier())).ifPresent(u -> auditService.logAction(u.getId(), "LOGIN_FAILED", null));
            throw new RuntimeException("Authentication failed");
        }
    }

    /**
     * Registers a new user account.
     *
     * @param request payload describing the username, email and password to register
     * @return confirmation message once the user is created
     * @throws IllegalArgumentException if the username or email already exists
     */
    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }


        User user = User.builder().username(request.getUsername()).email(request.getEmail()).passwordHash(encoder.encode(request.getPassword())).role(User.Role.USER).build();
        userRepository.save(user);
        auditService.logAction(user.getId(), "REGISTER", null);
        return "User registered successfully.";
    }

    /**
     * Updates the password of the specified user after validating the current password.
     *
     * @param username username of the account to update
     * @param request payload containing the current and new password values
     * @return confirmation message once the password has been updated
     * @throws IllegalArgumentException if the current password does not match the stored hash
     */
    public String updatePassword(String username, PasswordUpdateRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!encoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        user.setPasswordHash(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditService.logAction(user.getId(), "PASSWORD_UPDATED", null);
        return "Password updated successfully.";
    }
}
