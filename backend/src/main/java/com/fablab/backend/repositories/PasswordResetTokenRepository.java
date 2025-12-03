package com.fablab.backend.repositories;

import com.fablab.backend.models.PasswordResetToken;
import com.fablab.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository handling {@link PasswordResetToken} lifecycle operations.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    /**
     * Retrieves the most recent token issued for the supplied user email and
     * verification code.
     *
     * @param email account email address
     * @param code  verification code provided by the user
     * @return the newest matching token, if present
     */
    Optional<PasswordResetToken> findTopByUser_EmailAndCodeOrderByExpiresAtDesc(String email, String code);

    /**
     * Deletes all tokens belonging to the given user.
     *
     * @param user owner of the tokens to remove
     */
    void deleteByUser(User user);
}