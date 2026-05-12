package com.internship.tool.service;

import com.internship.tool.entity.ConsentRecord;
import com.internship.tool.entity.ConsentStatus;
import com.internship.tool.repository.ConsentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final ConsentRecordRepository repository;
    private final EmailService            emailService;

    @Value("${app.admin-email:admin@internship.com}")
    private String adminEmail;

    // ── DAILY DIGEST — 08:00 every day ───────────────────────
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyExpiryDigest() {
        log.info("Scheduler: running daily expiry digest");
        LocalDate today    = LocalDate.now();
        LocalDate deadline = today.plusDays(7);

        List<ConsentRecord> expiring =
            repository.findExpiringSoon(today, deadline);

        if (expiring.isEmpty()) {
            log.info("Scheduler: no records expiring "
                   + "in next 7 days — skipping digest");
            return;
        }

        emailService.sendDailyDigestEmail(
            adminEmail, expiring);
        log.info("Scheduler: daily digest triggered "
               + "for {} record(s)", expiring.size());
    }

    // ── INDIVIDUAL REMINDERS — 09:00 every day ───────────────
    @Scheduled(cron = "0 0 9 * * *")
    public void sendIndividualExpiryReminders() {
        log.info("Scheduler: running individual reminders");
        LocalDate today    = LocalDate.now();
        LocalDate deadline = today.plusDays(7);

        List<ConsentRecord> expiring =
            repository.findExpiringSoon(today, deadline);

        expiring.forEach(record -> {
            emailService.sendExpiryReminderEmail(record);
            log.info("Scheduler: reminder queued "
                   + "for id={}", record.getId());
        });
    }

    // ── AUTO-EXPIRE — midnight every day ─────────────────────
    @Scheduled(cron = "0 0 0 * * *")
    public void markExpiredRecords() {
        log.info("Scheduler: checking for expired records");
        LocalDate today = LocalDate.now();

        List<ConsentRecord> allActive =
            repository.findAllByStatusAndDeletedFalse(
                ConsentStatus.ACTIVE,
                Pageable.unpaged())
            .getContent();

        long count = allActive.stream()
            .filter(r -> r.getExpiryDate() != null
                      && r.getExpiryDate()
                           .isBefore(today))
            .peek(r -> {
                r.setStatus(ConsentStatus.EXPIRED);
                repository.save(r);
            })
            .count();

        log.info("Scheduler: {} record(s) marked EXPIRED",
                 count);
    }
}