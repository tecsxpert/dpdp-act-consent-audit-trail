package com.internship.tool.service;

import com.internship.tool.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditLogService {

    void log(String entityName,
             Long   entityId,
             String action,
             String changedFields,
             String performedBy,
             String ipAddress);

    List<AuditLog> getHistoryForRecord(
        String entityName, Long entityId);

    Page<AuditLog> getByPerformer(
        String performedBy, Pageable pageable);

    Page<AuditLog> getAll(Pageable pageable);
}