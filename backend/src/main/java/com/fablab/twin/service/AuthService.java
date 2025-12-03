package com.fablab.twin.service;

import com.fablab.twin.domain.dto.LoginRequest;
import com.fablab.twin.domain.dto.LoginResponse;
import com.fablab.twin.domain.model.User;
import com.fablab.twin.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, TokenEntry> activeTokens = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmailAndActiveTrue(request.email());
        User user = userOptional.filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        String token = UUID.randomUUID().toString();
        activeTokens.put(token, new TokenEntry(user.getId(), Instant.now().plus(8, ChronoUnit.HOURS)));
        Set<String> roleNames = user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
        return new LoginResponse(user.getId(), token, roleNames);
    }

    public UserDetails authenticateToken(String token) {
        TokenEntry entry = activeTokens.get(token);
        if (entry == null || entry.expiry().isBefore(Instant.now())) {
            activeTokens.remove(token);
            return null;
        }
        return userRepository.findById(entry.userId())
                .filter(User::isActive)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(), user.getPasswordHash(),
                        user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                                .toList()))
                .orElse(null);
    }

    private record TokenEntry(UUID userId, Instant expiry) {}
}