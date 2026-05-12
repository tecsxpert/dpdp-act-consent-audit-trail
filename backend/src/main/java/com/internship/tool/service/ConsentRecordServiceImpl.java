package com.internship.tool.service;

import com.internship.tool.config.AuditContext;
import com.internship.tool.config.CacheNames;
import com.internship.tool.entity.ConsentRecord;
import com.internship.tool.entity.ConsentStatus;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.exception.ValidationException;
import com.internship.tool.repository.ConsentRecordRepository;
import com.internship.tool.service.dto.ConsentRecordRequest;
import com.internship.tool.service.dto.ConsentRecordResponse;
import com.internship.tool.service.dto.StatsResponse;
import com.internship.tool.service.util.ChangedFieldsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentRecordServiceImpl
        implements ConsentRecordService {

    // ============================================================
    // FIELDS
    // ============================================================

    private final ConsentRecordRepository repository;
    private final ConsentRecordMapper     mapper;
    private final EmailService            emailService;
    private final AuditLogService         auditLogService;
    private final AuditContext            auditContext;
    private final AiIntegrationService    aiIntegrationService;

    // ============================================================
    // READ
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = CacheNames.CONSENT_RECORDS,
        key   = "#pageable.pageNumber + '_' "
              + "+ #pageable.pageSize + '_' "
              + "+ #pageable.sort"
    )
    public Page<ConsentRecordResponse> getAll(
            Pageable pageable) {
        log.debug("Cache MISS — fetching all records from DB");
        return repository
            .findAllByDeletedFalse(pageable)
            .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = CacheNames.CONSENT_RECORD_BY_ID,
        key   = "#id"
    )
    public ConsentRecordResponse getById(Long id) {
        log.debug("Cache MISS — fetching record id={} "
                + "from DB", id);
        return mapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = CacheNames.CONSENT_SEARCH,
        key   = "#query + '_' "
              + "+ #pageable.pageNumber + '_' "
              + "+ #pageable.pageSize"
    )
    public Page<ConsentRecordResponse> search(
            String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return getAll(pageable);
        }
        return repository
            .searchByQuery(query.trim(), pageable)
            .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = CacheNames.CONSENT_RECORDS,
        key   = "'status_' + #status + '_' "
              + "+ #pageable.pageNumber"
    )
    public Page<ConsentRecordResponse> filterByStatus(
            ConsentStatus status,
            Pageable pageable) {
        return repository
            .findAllByStatusAndDeletedFalse(status, pageable)
            .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = CacheNames.CONSENT_STATS,
        key   = "'global_stats'"
    )
    public StatsResponse getStats() {
        log.debug("Cache MISS — computing stats from DB");
        return StatsResponse.builder()
            .total(repository.countByDeletedFalse())
            .active(repository
                .countByStatusAndDeletedFalse(
                    ConsentStatus.ACTIVE))
            .withdrawn(repository
                .countByStatusAndDeletedFalse(
                    ConsentStatus.WITHDRAWN))
            .expired(repository
                .countByStatusAndDeletedFalse(
                    ConsentStatus.EXPIRED))
            .pending(repository
                .countByStatusAndDeletedFalse(
                    ConsentStatus.PENDING))
            .revoked(repository
                .countByStatusAndDeletedFalse(
                    ConsentStatus.REVOKED))
            .aiProcessed(repository.findAll().stream()
                .filter(c ->
                    Boolean.TRUE.equals(c.getAiProcessed())
                    && !Boolean.TRUE.equals(c.getDeleted()))
                .count())
            .build();
    }

    // ============================================================
    // WRITE
    // ============================================================

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(
            value      = CacheNames.CONSENT_RECORDS,
            allEntries = true),
        @CacheEvict(
            value      = CacheNames.CONSENT_STATS,
            allEntries = true),
        @CacheEvict(
            value      = CacheNames.CONSENT_SEARCH,
            allEntries = true)
    })
    public ConsentRecordResponse create(
            ConsentRecordRequest request) {

        validateDates(request);
        ConsentRecord record = mapper.toEntity(request);
        ConsentRecord saved  = repository.save(record);

        // ── audit CREATE ─────────────────────────────────────
        auditLogService.log(
            "ConsentRecord",
            saved.getId(),
            "CREATE",
            ChangedFieldsUtil.snapshot(
                mapper.toResponse(saved)),
            auditContext.currentUser(),
            auditContext.clientIp()
        );

        // ── trigger AI analysis async ────────────────────────
        // runs in background thread — does not block response
        aiIntegrationService.triggerAnalysis(saved);

        log.info("Created ConsentRecord id={} "
               + "— caches evicted, AI triggered",
                 saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(
        put = {
            @CachePut(
                value = CacheNames.CONSENT_RECORD_BY_ID,
                key   = "#id")
        },
        evict = {
            @CacheEvict(
                value      = CacheNames.CONSENT_RECORDS,
                allEntries = true),
            @CacheEvict(
                value      = CacheNames.CONSENT_STATS,
                allEntries = true),
            @CacheEvict(
                value      = CacheNames.CONSENT_SEARCH,
                allEntries = true)
        }
    )
    public ConsentRecordResponse update(
            Long id,
            ConsentRecordRequest request) {

        validateDates(request);
        ConsentRecord existing = findActiveOrThrow(id);

        // ── capture before-state for diff ────────────────────
        Map<String, Object[]> changes = new LinkedHashMap<>();

        if (!existing.getStatus()
                     .equals(request.getStatus())) {
            changes.put("status", new Object[]{
                existing.getStatus(),
                request.getStatus()
            });
        }
        if (!existing.getPurpose()
                     .equals(request.getPurpose())) {
            changes.put("purpose", new Object[]{
                existing.getPurpose(),
                request.getPurpose()
            });
        }
        if (!existing.getSubjectEmail().equals(
                request.getSubjectEmail()
                       .trim().toLowerCase())) {
            changes.put("subjectEmail", new Object[]{
                existing.getSubjectEmail(),
                request.getSubjectEmail()
                       .trim().toLowerCase()
            });
        }
        if (request.getConsentDate() != null
                && !request.getConsentDate()
                           .equals(existing
                               .getConsentDate())) {
            changes.put("consentDate", new Object[]{
                existing.getConsentDate(),
                request.getConsentDate()
            });
        }
        if (request.getExpiryDate() != null
                && !request.getExpiryDate()
                           .equals(existing
                               .getExpiryDate())) {
            changes.put("expiryDate", new Object[]{
                existing.getExpiryDate(),
                request.getExpiryDate()
            });
        }

        // ── set withdrawal date if status → WITHDRAWN ────────
        if (request.getStatus() == ConsentStatus.WITHDRAWN
                && existing.getWithdrawalDate() == null) {
            existing.setWithdrawalDate(LocalDate.now());
            changes.put("withdrawalDate", new Object[]{
                null,
                LocalDate.now()
            });
        }

        mapper.updateEntity(existing, request);
        ConsentRecord saved = repository.save(existing);

        // ── trigger withdrawal email async ───────────────────
        if (saved.getStatus() == ConsentStatus.WITHDRAWN) {
            emailService
                .sendWithdrawalConfirmationEmail(saved);
            log.info("Withdrawal email queued for id={}",
                     saved.getId());
        }

        // ── audit UPDATE ─────────────────────────────────────
        auditLogService.log(
            "ConsentRecord",
            saved.getId(),
            "UPDATE",
            ChangedFieldsUtil.diff(changes),
            auditContext.currentUser(),
            auditContext.clientIp()
        );

        log.info("Updated ConsentRecord id={} "
               + "— cache updated", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(
            value = CacheNames.CONSENT_RECORD_BY_ID,
            key   = "#id"),
        @CacheEvict(
            value      = CacheNames.CONSENT_RECORDS,
            allEntries = true),
        @CacheEvict(
            value      = CacheNames.CONSENT_STATS,
            allEntries = true),
        @CacheEvict(
            value      = CacheNames.CONSENT_SEARCH,
            allEntries = true)
    })
    public void delete(Long id) {
        ConsentRecord existing = findActiveOrThrow(id);
        existing.setDeleted(true);
        existing.setDeletedAt(LocalDate.now());
        repository.save(existing);

        // ── audit DELETE ─────────────────────────────────────
        auditLogService.log(
            "ConsentRecord",
            id,
            "DELETE",
            ChangedFieldsUtil.snapshot(Map.of(
                "deletedAt",
                LocalDate.now().toString(),
                "deletedBy",
                auditContext.currentUser()
            )),
            auditContext.currentUser(),
            auditContext.clientIp()
        );

        log.info("Soft-deleted ConsentRecord id={} "
               + "— caches evicted", id);
    }

    // ============================================================
    // ADMIN
    // ============================================================

    @Override
    @Caching(evict = {
        @CacheEvict(
            value      = CacheNames.CONSENT_RECORDS,
            allEntries = true),
        @CacheEvict(
            value      = CacheNames.CONSENT_RECORD_BY_ID,
            allEntries = true),
        @CacheEvict(
            value      = CacheNames.CONSENT_STATS,
            allEntries = true),
        @CacheEvict(
            value      = CacheNames.CONSENT_SEARCH,
            allEntries = true)
    })
    public void evictAllCaches() {
        log.info("All caches manually evicted by admin");
    }

    // ============================================================
    // AI — legacy direct attach
    // (used by AiIntegrationService.attachResults)
    // ============================================================

    @Async
    @Transactional
    @Caching(evict = {
        @CacheEvict(
            value = CacheNames.CONSENT_RECORD_BY_ID,
            key   = "#id"),
        @CacheEvict(
            value      = CacheNames.CONSENT_RECORDS,
            allEntries = true)
    })
    public void attachAiResults(
            Long   id,
            String description,
            String recommendations) {
        repository.findByIdAndDeletedFalse(id)
            .ifPresent(record -> {
                record.setAiDescription(description);
                record.setAiRecommendations(recommendations);
                record.setAiProcessed(true);
                repository.save(record);
                log.info("AI results attached to id={} "
                       + "— cache evicted", id);
            });
    }

    // ============================================================
    // HELPERS
    // ============================================================

    public ConsentRecord findActiveOrThrow(Long id) {
        return repository.findByIdAndDeletedFalse(id)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "ConsentRecord", id));
    }

    private void validateDates(
            ConsentRecordRequest req) {
        if (req.getConsentDate() != null
                && req.getExpiryDate() != null
                && req.getExpiryDate()
                       .isBefore(req.getConsentDate())) {
            throw new ValidationException(Map.of(
                "expiryDate",
                "Expiry date must be on or after "
              + "consent date"
            ));
        }
    }
}