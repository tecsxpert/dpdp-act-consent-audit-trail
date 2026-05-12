package com.internship.tool.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // ── signing key ──────────────────────────────────────────
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ── generate ─────────────────────────────────────────────
    public String generateAccessToken(String email,
                                       Set<String> roles) {
        return Jwts.builder()
            .subject(email)
            .claim("roles", roles)
            .claim("type", "access")
            .issuedAt(new Date())
            .expiration(new Date(
                System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
            .subject(email)
            .claim("type", "refresh")
            .issuedAt(new Date())
            .expiration(new Date(
                System.currentTimeMillis() + refreshExpirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    // ── extract ──────────────────────────────────────────────
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractTokenType(String token) {
        return (String) extractAllClaims(token).get("type");
    }

    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Object roles = extractAllClaims(token).get("roles");
        if (roles instanceof java.util.List<?> list) {
            return new java.util.HashSet<>(
                list.stream()
                    .map(Object::toString)
                    .toList());
        }
        return Set.of();
    }

    private <T> T extractClaim(String token,
                                 Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    // ── validate ─────────────────────────────────────────────
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT empty: {}", e.getMessage());
        }
        return false;
    }

    public boolean isTokenValid(String token, String email) {
        try {
            return extractEmail(token).equals(email)
                && !extractExpiration(token)
                       .before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}",
                     e.getMessage());
            return false;
        }
    }
}