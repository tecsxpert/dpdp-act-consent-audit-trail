package com.internship.tool.controller;

import com.internship.tool.service.AuthService;
import com.internship.tool.service.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation
    .AuthenticationPrincipal;
import org.springframework.security.core.userdetails
    .UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication",
     description = "Register, login and refresh token")
public class AuthController {

    private final AuthService authService;

    // ── REGISTER ─────────────────────────────────────────────
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
                     description = "User registered successfully"),
        @ApiResponse(responseCode = "400",
                     description = "Validation error"),
        @ApiResponse(responseCode = "409",
                     description = "Email already registered")
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(authService.register(request));
    }

    // ── LOGIN ────────────────────────────────────────────────
    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT tokens")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                     description = "Login successful"),
        @ApiResponse(responseCode = "401",
                     description = "Invalid credentials"),
        @ApiResponse(responseCode = "400",
                     description = "Validation error")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ── REFRESH ──────────────────────────────────────────────
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                     description = "Token refreshed"),
        @ApiResponse(responseCode = "401",
                     description = "Invalid or expired token")
    })
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    // ── ME ───────────────────────────────────────────────────
    @GetMapping("/me")
    @Operation(summary = "Get current logged-in user info")
    @ApiResponse(responseCode = "200",
                 description = "Current user returned")
    public ResponseEntity<Map<String, Object>> me(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
            "email",       userDetails.getUsername(),
            "authorities", userDetails.getAuthorities()
        ));
    }
  // ── TEMP — remove after use ──────────────────────────────
@GetMapping("/hash")
public ResponseEntity<String> generateHash(
        @RequestParam String password) {
    return ResponseEntity.ok(
        new org.springframework.security.crypto
            .bcrypt.BCryptPasswordEncoder()
            .encode(password));
}
}