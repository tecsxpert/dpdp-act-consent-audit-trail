package com.internship.tool.service;

import com.internship.tool.entity.AuditLog;
import com.internship.tool.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    // ── WRITE — always @Async so it never blocks ─────────────
    @Async
    @Override
    @Transactional
    public void log(String entityName,
                    Long   entityId,
                    String action,
                    String changedFields,
                    String performedBy,
                    String ipAddress) {
        try {
            AuditLog entry = AuditLog.builder()
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .changedFields(changedFields)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .build();

            auditLogRepository.save(entry);
            log.debug("Audit logged: {} {} id={} by {}",
                      action, entityName,
                      entityId, performedBy);
        } catch (Exception e) {
            // NEVER let audit failure break main transaction
            log.error("Audit log write failed for "
                    + "{} id={}: {}",
                      entityName, entityId,
                      e.getMessage());
        }
    }

    // ── READ ─────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getHistoryForRecord(
            String entityName, Long entityId) {
        return auditLogRepository
            .findByEntityNameAndEntityIdOrderByPerformedAtDesc(
                entityName, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getByPerformer(
            String performedBy, Pageable pageable) {
        return auditLogRepository
            .findByPerformedByOrderByPerformedAtDesc(
                performedBy, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}