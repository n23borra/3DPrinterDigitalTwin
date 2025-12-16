package com.fablab.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupValidationConfig {

    @Bean
    ApplicationRunner validateRequiredConfig(@Value("${jwt.secret:}") String jwtSecret) {
        return args -> {
            if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
                throw new IllegalStateException(
                        "Missing JWT secret: set JWT_SECRET in a .env file (see .env.example)."
                );
            }

            // Simple guardrail (length in chars). For HMAC JWT, 32+ chars is a reasonable minimum.
            if (jwtSecret.trim().length() < 32) {
                throw new IllegalStateException(
                        "JWT_SECRET is too short. Use at least 32 characters (prefer: openssl rand -base64 32)."
                );
            }
        };
    }
}
