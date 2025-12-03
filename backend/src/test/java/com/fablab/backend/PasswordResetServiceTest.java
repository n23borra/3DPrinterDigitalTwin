package com.fablab.backend;

import com.fablab.backend.models.PasswordResetToken;
import com.fablab.backend.models.User;
import com.fablab.backend.repositories.PasswordResetTokenRepository;
import com.fablab.backend.repositories.UserRepository;
import com.fablab.backend.services.AuditLogService;
import com.fablab.backend.services.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DataJpaTest
@Import({PasswordResetService.class, PasswordResetServiceTest.Config.class, AuditLogService.class})
class PasswordResetServiceTest {

    @TestConfiguration
    static class Config {
        @Bean
        PasswordEncoder encoder() { return new BCryptPasswordEncoder(); }
        @Bean
        JavaMailSender mailSender() { return mock(JavaMailSender.class); }
    }

    @Autowired
    private PasswordResetService service;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordResetTokenRepository tokenRepo;
    @Autowired
    private PasswordEncoder encoder;

    @Test
    void createsTokenForEmail() {
        User u = User.builder().username("u").email("e@test.com").passwordHash("p").role(User.Role.USER).build();
        userRepo.save(u);
        service.createToken("e@test.com");
        List<PasswordResetToken> list = tokenRepo.findAll();
        assertEquals(1, list.size());
        assertNotNull(list.get(0).getCode());
    }

    @Test
    void resetPasswordUpdatesHash() {
        User u = User.builder().username("u").email("e@test.com").passwordHash("old").role(User.Role.USER).build();
        userRepo.save(u);
        service.createToken("e@test.com");
        PasswordResetToken token = tokenRepo.findAll().get(0);
        service.resetPassword("e@test.com", token.getCode(), "newpass");
        User updated = userRepo.findByEmail("e@test.com").orElseThrow();
        assertTrue(encoder.matches("newpass", updated.getPasswordHash()));
        assertEquals(0, tokenRepo.count());
    }
}