package com.fablab.backend.controllers;

import com.fablab.backend.models.User;
import com.fablab.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    /**
     * Returns the profile of the authenticated user.
     *
     * @param authentication security context containing the authenticated principal
     * @return {@link ResponseEntity} with the user information, {@code 401 Unauthorized} if no user is authenticated,
     *         or {@code 404 Not Found} when the user record cannot be located
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        log.info("/me requested by {} with authorities {}", username, authentication.getAuthorities());
        Optional<User> user = userRepository.findByUsername(username);

        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lists all registered users.
     *
     * @return {@link ResponseEntity} containing the collection of users
     */
    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
