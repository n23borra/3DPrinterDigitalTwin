package com.fablab.twin.controller;

import com.fablab.twin.domain.model.User;
import com.fablab.twin.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return Map.of();
        }
        User user = userRepository.findByEmailAndActiveTrue(principal.getUsername()).orElseThrow();
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "roles", user.getRoles().stream().map(r -> r.getName()).toList()
        );
    }
}