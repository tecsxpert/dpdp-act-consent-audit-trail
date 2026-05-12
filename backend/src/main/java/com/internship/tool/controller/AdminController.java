package com.internship.tool.controller;

import com.internship.tool.entity.User;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin",
     description = "Admin-only operations")
public class AdminController {

    private final UserRepository userRepository;

    // ── GET ALL USERS ────────────────────────────────────────
    @GetMapping("/users")
    @Operation(summary = "List all users (ADMIN only)")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ── UPDATE ROLES ─────────────────────────────────────────
    @PutMapping("/users/{id}/roles")
    @Operation(summary = "Update user roles (ADMIN only)")
    public ResponseEntity<Map<String, String>> updateRoles(
            @PathVariable Long id,
            @RequestBody Map<String, Set<String>> body) {

        User user = userRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("User", id));

        user.setRoles(body.get("roles"));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "message",
            "Roles updated for user id: " + id));
    }
}