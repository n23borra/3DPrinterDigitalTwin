package com.fablab.backend.services;

import com.fablab.backend.models.User;
import com.fablab.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    /**
     * Loads a user by username or email for Spring Security authentication.
     *
     * @param identifier username or email provided during authentication
     * @return {@link UserDetails} describing the authenticated user
     * @throws UsernameNotFoundException if no user matches the provided identifier
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(identifier).or(() -> userRepository.findByEmail(identifier)).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(), List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}
