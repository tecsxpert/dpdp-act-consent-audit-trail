package com.internship.tool.service;

import com.internship.tool.config.JwtUtil;
import com.internship.tool.entity.User;
import com.internship.tool.exception.DuplicateResourceException;
import com.internship.tool.exception.UnauthorizedException;
import com.internship.tool.repository.UserRepository;
import com.internship.tool.service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication
    .AuthenticationManager;
import org.springframework.security.authentication
    .BadCredentialsException;
import org.springframework.security.authentication
    .UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password
    .PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ── REGISTER ─────────────────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                "Email already registered: "
                + request.getEmail());
        }

        User user = User.builder()
            .fullName(request.getFullName().trim())
            .email(request.getEmail()
                          .trim().toLowerCase())
            .password(passwordEncoder.encode(
                          request.getPassword()))
            .roles(Set.of("ROLE_USER"))
            .enabled(true)
            .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ── LOGIN ────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail()
                           .trim().toLowerCase(),
                    request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException(
                "Invalid email or password");
        }

        User user = userRepository
            .findByEmail(request.getEmail()
                                .trim().toLowerCase())
            .orElseThrow(() ->
                new UnauthorizedException("User not found"));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ── REFRESH ──────────────────────────────────────────────
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException(
                "Invalid or expired refresh token");
        }
        if (!"refresh".equals(jwtUtil.extractTokenType(token))) {
            throw new UnauthorizedException(
                "Token is not a refresh token");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                new UnauthorizedException("User not found"));

        log.info("Token refreshed for: {}", email);
        return buildAuthResponse(user);
    }

    // ── helper ───────────────────────────────────────────────
    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtUtil.generateAccessToken(
            user.getEmail(), user.getRoles());
        String refreshToken = jwtUtil.generateRefreshToken(
            user.getEmail());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(86400000L)
            .email(user.getEmail())
            .fullName(user.getFullName())
            .roles(user.getRoles())
            .build();
    }
}