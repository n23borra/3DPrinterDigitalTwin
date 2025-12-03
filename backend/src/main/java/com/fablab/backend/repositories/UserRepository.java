package com.fablab.backend.repositories;

import com.fablab.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository providing lookup operations for {@link User} accounts.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by username.
     *
     * @param username username supplied by the client
     * @return matching user when present
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email address.
     *
     * @param email email supplied by the client
     * @return matching user when present
     */
    Optional<User> findByEmail(String email);

}
