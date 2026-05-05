package com.internship.tool.service;

import com.internship.tool.entity.AuditLog;
import com.internship.tool.entity.ConsentRecord;
import com.internship.tool.repository.AuditLogRepository;
import com.internship.tool.repository.ConsentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConsentRecordService {

    private final ConsentRecordRepository consentRecordRepository;
    private final AuditLogRepository auditLogRepository;
    private final AiServiceClient aiServiceClient;
    private final EmailService emailService;

    public Page<ConsentRecord> getAllRecords(
            String q, String status,
            String from, String to,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        LocalDateTime fromDate = (from != null && !from.isEmpty())
                ? LocalDate.parse(from).atStartOfDay() : null;
        LocalDateTime toDate = (to != null && !to.isEmpty())
                ? LocalDate.parse(to).atTime(23, 59, 59) : null;

        String searchQ = (q != null && !q.isEmpty()) ? q : null;
        String searchStatus = (status != null && !status.isEmpty()) ? status : null;

        return consentRecordRepository.searchRecords(
                searchQ, searchStatus, fromDate, toDate, pageable);
    }

    public Optional<ConsentRecord> getById(Long id) {
        return consentRecordRepository.findById(id)
                .filter(ConsentRecord::getIsActive);
    }

    public ConsentRecord create(ConsentRecord record, String performedBy) {
        record.setIsActive(true);
        record.setConsentStatus("PENDING");
        ConsentRecord saved = consentRecordRepository.save(record);

        logAudit(saved.getId(), "CREATE", performedBy,
                null, saved.getConsentStatus(), "Record created");

        // send email notification
        emailService.sendConsentCreatedEmail(saved);

        // call AI service in background thread so response is not delayed
        new Thread(() -> aiServiceClient.enrichWithAiDescription(
                saved, this)).start();

        return saved;
    }

    public ConsentRecord update(Long id, ConsentRecord updated, String performedBy) {
        ConsentRecord existing = consentRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        String oldStatus = existing.getConsentStatus();

        existing.setDataPrincipalId(updated.getDataPrincipalId());
        existing.setDataPrincipalName(updated.getDataPrincipalName());
        existing.setDataPrincipalEmail(updated.getDataPrincipalEmail());
        existing.setDataFiduciaryId(updated.getDataFiduciaryId());
        existing.setDataFiduciaryName(updated.getDataFiduciaryName());
        existing.setPurpose(updated.getPurpose());
        existing.setDataCategories(updated.getDataCategories());
        existing.setConsentDate(updated.getConsentDate());
        existing.setExpiryDate(updated.getExpiryDate());

        if (!oldStatus.equals(updated.getConsentStatus())) {
            existing.setConsentStatus(updated.getConsentStatus());
            logAudit(id, "STATUS_CHANGE", performedBy,
                    oldStatus, updated.getConsentStatus(), "Status updated");

            // send email based on new status
            if ("GRANTED".equals(updated.getConsentStatus())) {
                emailService.sendConsentGrantedEmail(existing);
            } else if ("REVOKED".equals(updated.getConsentStatus())) {
                emailService.sendConsentRevokedEmail(existing);
            }
        } else {
            logAudit(id, "UPDATE", performedBy,
                    null, null, "Record updated");
        }

        return consentRecordRepository.save(existing);
    }

    public void delete(Long id, String performedBy) {
        ConsentRecord record = consentRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        record.setIsActive(false);
        consentRecordRepository.save(record);

        logAudit(id, "DELETE", performedBy,
                null, null, "Record soft deleted");
    }

    public ConsentRecord updateAiFields(Long id, String description,
                                         Integer score, Boolean isFallback) {
        ConsentRecord record = consentRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        record.setAiDescription(description);
        record.setAiScore(score);
        record.setIsFallback(isFallback);

        return consentRecordRepository.save(record);
    }

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", consentRecordRepository.countByIsActiveTrue());
        stats.put("granted", consentRecordRepository
                .countByConsentStatusAndIsActiveTrue("GRANTED"));
        stats.put("revoked", consentRecordRepository
                .countByConsentStatusAndIsActiveTrue("REVOKED"));
        stats.put("pending", consentRecordRepository
                .countByConsentStatusAndIsActiveTrue("PENDING"));
        stats.put("expired", consentRecordRepository
                .countByConsentStatusAndIsActiveTrue("EXPIRED"));
        return stats;
    }

    private void logAudit(Long recordId, String action,
                           String performedBy, String oldValue,
                           String newValue, String remarks) {
        AuditLog log = AuditLog.builder()
                .consentRecordId(recordId)
                .action(action)
                .performedBy(performedBy)
                .oldValue(oldValue)
                .newValue(newValue)
                .remarks(remarks)
                .build();
        auditLogRepository.save(log);
    }
}