package com.fablab.backend.services;

import com.fablab.backend.models.PasswordResetToken;
import com.fablab.backend.models.User;
import com.fablab.backend.repositories.PasswordResetTokenRepository;
import com.fablab.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder encoder;
    private final AuditLogService auditService;

    /**
     * Creates a password reset token for the user associated with the provided email and dispatches it by mail.
     *
     * @param email user email that should receive the reset code
     * @throws IllegalArgumentException if the email does not correspond to an existing user
     */
    public void createToken(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCode(code);
        token.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        tokenRepo.save(token);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail());
        msg.setSubject("Password reset code");
        msg.setText("Your reset code is: " + code);
        mailSender.send(msg);
    }

    /**
     * Resets a user's password using a previously generated token.
     *
     * @param email       email of the account whose password must be reset
     * @param code        reset code received by the user
     * @param newPassword new password to persist for the user
     * @throws IllegalArgumentException if the code is invalid or expired
     */
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        PasswordResetToken token = tokenRepo
                .findTopByUser_EmailAndCodeOrderByExpiresAtDesc(email, code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid code"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Code expired");
        }
        User user = token.getUser();
        user.setPasswordHash(encoder.encode(newPassword));
        userRepo.save(user);
        tokenRepo.deleteByUser(user);
        auditService.logAction(user.getId(), "PASSWORD_RESET", null);
    }
}