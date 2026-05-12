package com.internship.tool.controller;

import com.internship.tool.entity.ConsentStatus;
import com.internship.tool.service.AiIntegrationService;
import com.internship.tool.service.ConsentRecordService;
import com.internship.tool.service.dto.ConsentRecordRequest;
import com.internship.tool.service.dto.ConsentRecordResponse;
import com.internship.tool.service.dto.StatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/consent-records")
@RequiredArgsConstructor
@Tag(name = "Consent Records",
     description = "CRUD operations for consent audit trail")
public class ConsentRecordController {

    private final ConsentRecordService service;
    private final AiIntegrationService aiIntegrationService;

    // ── GET ALL ──────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get all consent records (paginated)")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                     description = "Records retrieved"),
        @ApiResponse(responseCode = "401",
                     description = "Unauthorized"),
        @ApiResponse(responseCode = "403",
                     description = "Forbidden")
    })
    public ResponseEntity<Page<ConsentRecordResponse>> getAll(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0")
                int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10")
                int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt")
                String sortBy,
            @Parameter(description = "asc or desc")
            @RequestParam(defaultValue = "desc")
                String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable =
            PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
            service.getAll(pageable));
    }

    // ── GET BY ID ────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get a consent record by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                     description = "Record found"),
        @ApiResponse(responseCode = "404",
                     description = "Not found"),
        @ApiResponse(responseCode = "401",
                     description = "Unauthorized")
    })
    public ResponseEntity<ConsentRecordResponse> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ── POST CREATE ──────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Create a new consent record")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
                     description = "Created"),
        @ApiResponse(responseCode = "400",
                     description = "Validation error"),
        @ApiResponse(responseCode = "401",
                     description = "Unauthorized")
    })
    public ResponseEntity<ConsentRecordResponse> create(
            @Valid @RequestBody
                ConsentRecordRequest request) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(service.create(request));
    }

    // ── PUT UPDATE ───────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Update a consent record")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                     description = "Updated"),
        @ApiResponse(responseCode = "400",
                     description = "Validation error"),
        @ApiResponse(responseCode = "404",
                     description = "Not found"),
        @ApiResponse(responseCode = "401",
                     description = "Unauthorized")
    })
    public ResponseEntity<ConsentRecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody
                ConsentRecordRequest request) {
        return ResponseEntity.ok(
            service.update(id, request));
    }

    // ── DELETE (soft) — ADMIN only ───────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary =
        "Soft-delete a record (ADMIN only)")
    @ApiResponses({
        @ApiResponse(responseCode = "204",
                     description = "Deleted"),
        @ApiResponse(responseCode = "404",
                     description = "Not found"),
        @ApiResponse(responseCode = "403",
                     description = "Forbidden — ADMIN only")
    })
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── SEARCH ───────────────────────────────────────────────
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Search by name, email or purpose")
    @ApiResponse(responseCode = "200",
                 description = "Search results returned")
    public ResponseEntity<Page<ConsentRecordResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(
            page, size,
            Sort.by("createdAt").descending());
        return ResponseEntity.ok(
            service.search(q, pageable));
    }

    // ── FILTER ───────────────────────────────────────────────
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Filter by status")
    @ApiResponse(responseCode = "200",
                 description = "Filtered results returned")
    public ResponseEntity<Page<ConsentRecordResponse>> filterByStatus(
            @RequestParam ConsentStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(
            page, size,
            Sort.by("createdAt").descending());
        return ResponseEntity.ok(
            service.filterByStatus(status, pageable));
    }

    // ── STATS ────────────────────────────────────────────────
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get dashboard KPI stats")
    @ApiResponse(responseCode = "200",
                 description = "Stats returned")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(service.getStats());
    }

    // ── CACHE EVICT — ADMIN only ─────────────────────────────
    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary =
        "Manually evict all caches (ADMIN only)")
    @ApiResponse(responseCode = "204",
                 description = "Caches cleared")
    public ResponseEntity<Void> evictCaches() {
        service.evictAllCaches();
        return ResponseEntity.noContent().build();
    }

    // ── AI RETRY — ADMIN only ────────────────────────────────
    @PostMapping("/{id}/ai-retry")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary =
        "Manually trigger AI re-analysis (ADMIN only)")
    @ApiResponses({
        @ApiResponse(responseCode = "202",
                     description = "AI retry queued"),
        @ApiResponse(responseCode = "404",
                     description = "Record not found"),
        @ApiResponse(responseCode = "403",
                     description = "Forbidden — ADMIN only")
    })
    public ResponseEntity<Void> retryAiAnalysis(
            @PathVariable Long id) {
        // verify record exists — throws 404 if not found
        service.getById(id);
        aiIntegrationService.retryAnalysis(id);
        // 202 Accepted — async, result not immediate
        return ResponseEntity.accepted().build();
    }
}