package com.fablab.twin.service;

import com.fablab.twin.domain.model.User;
import com.fablab.twin.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User create(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setActive(true);
        return userRepository.save(user);
    }

    public void deactivate(UUID id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setActive(false);
            userRepository.save(u);
        });
    }
}