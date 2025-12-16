package com.fablab.backend.security;

import com.fablab.backend.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Handles JWT creation, parsing and validation using the shared secret defined
 * in application configuration.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;
    private final long jwtExpiration = 86400000; // 24h

    private Key secretKey;

    /**
     * Initializes the signing key once the secret value has been injected.
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a signed JWT for the authenticated user.
     *
     * @param user the authenticated principal
     * @return a compact JWT string containing the username subject
     */
    public String generateToken(User user) {
        return Jwts.builder().setSubject(user.getUsername()).setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)).signWith(secretKey, SignatureAlgorithm.HS256).compact();
    }

    /**
     * Retrieves the username from the provided token.
     *
     * @param token the JWT to parse
     * @return the subject embedded within the token
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Verifies that the token signature and expiration are valid.
     *
     * @param token the JWT presented by the client
     * @return {@code true} when the token can be parsed successfully; {@code false}
     * otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses the token payload and returns the decoded claims.
     *
     * @param token the JWT to inspect
     * @return the claims extracted from the token body
     * @throws JwtException if the token cannot be verified
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }
}
