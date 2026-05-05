package com.internship.tool.service;

import com.internship.tool.entity.ConsentRecord;
import com.internship.tool.repository.ConsentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailScheduler {

    private final ConsentRecordRepository consentRecordRepository;
    private final EmailService emailService;

    // runs every day at 9 AM
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void sendDailyReminders() {
        System.out.println("📧 Running daily consent reminder job...");

        // find records expiring in the next 7 days
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);

        List<ConsentRecord> expiringRecords = consentRecordRepository
                .findAll(PageRequest.of(0, 1000))
                .getContent()
                .stream()
                .filter(r -> r.getIsActive()
                        && r.getExpiryDate() != null
                        && r.getExpiryDate().isAfter(now)
                        && r.getExpiryDate().isBefore(sevenDaysLater))
                .toList();

        expiringRecords.forEach(record -> {
            try {
                emailService.sendExpiryReminderEmail(record);
            } catch (Exception e) {
                System.out.println("⚠️ Failed to send reminder: " + e.getMessage());
            }
        });

        System.out.println("📧 Daily reminder job complete. Sent " 
                + expiringRecords.size() + " reminders.");
    }
}