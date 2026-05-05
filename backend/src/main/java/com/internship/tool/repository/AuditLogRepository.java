package com.internship.tool.repository;

import com.internship.tool.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // get all audit logs for a specific consent record
    List<AuditLog> findByConsentRecordIdOrderByPerformedAtDesc(Long consentRecordId);

    // get paginated audit logs for a specific consent record
    Page<AuditLog> findByConsentRecordId(Long consentRecordId, Pageable pageable);
}