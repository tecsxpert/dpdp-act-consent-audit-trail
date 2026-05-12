package com.internship.tool.controller;

import com.internship.tool.entity.AuditLog;
import com.internship.tool.service.AuditLogService;
import com.internship.tool.service.dto.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Logs",
     description = "View audit history — ADMIN only")
public class AuditLogController {

    private final AuditLogService auditLogService;

    // ── GET ALL ──────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Get all audit logs paginated")
    public ResponseEntity<Page<AuditLogResponse>> getAll(
            @RequestParam(defaultValue = "0")           int page,
            @RequestParam(defaultValue = "20")          int size,
            @RequestParam(defaultValue = "performedAt") String sortBy,
            @RequestParam(defaultValue = "desc")        String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
            auditLogService.getAll(pageable)
                           .map(this::toResponse));
    }

    // ── GET BY RECORD ────────────────────────────────────────
    @GetMapping("/record/{entityName}/{entityId}")
    @Operation(summary = "Get audit history for a specific record")
    public ResponseEntity<List<AuditLogResponse>> getForRecord(
            @PathVariable String entityName,
            @PathVariable Long   entityId) {

        List<AuditLogResponse> logs =
            auditLogService
                .getHistoryForRecord(entityName, entityId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(logs);
    }

    // ── GET BY PERFORMER ─────────────────────────────────────
    @GetMapping("/performer")
    @Operation(summary = "Get audit logs by performer email")
    public ResponseEntity<Page<AuditLogResponse>> getByPerformer(
            @RequestParam                      String performedBy,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
            Sort.by("performedAt").descending());
        return ResponseEntity.ok(
            auditLogService
                .getByPerformer(performedBy, pageable)
                .map(this::toResponse));
    }

    // ── mapper ───────────────────────────────────────────────
    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
            .id(log.getId())
            .entityName(log.getEntityName())
            .entityId(log.getEntityId())
            .action(log.getAction())
            .changedFields(log.getChangedFields())
            .performedBy(log.getPerformedBy())
            .performedAt(log.getPerformedAt())
            .ipAddress(log.getIpAddress())
            .build();
    }
}