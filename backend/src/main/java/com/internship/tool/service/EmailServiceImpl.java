package com.internship.tool.service;

import com.internship.tool.entity.ConsentRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition
    .ConditionalOnBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    // Optional injection — null if mail not configured
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    // ── EXPIRY REMINDER ──────────────────────────────────────
    @Async
    @Override
    public void sendExpiryReminderEmail(ConsentRecord record) {
        if (mailSender == null) {
            log.warn("Mail not configured — skipping "
                   + "expiry reminder for id={}",
                     record.getId());
            return;
        }
        try {
            Context ctx = new Context();
            ctx.setVariable("subjectName",
                            record.getSubjectName());
            ctx.setVariable("subjectEmail",
                            record.getSubjectEmail());
            ctx.setVariable("purpose",
                            record.getPurpose());
            ctx.setVariable("expiryDate",
                            record.getExpiryDate());
            ctx.setVariable("recordId",
                            record.getId());

            String html = templateEngine.process(
                "email/expiry-reminder", ctx);
            sendHtml(
                record.getSubjectEmail(),
                "Your consent is expiring soon",
                html);

            log.info("Expiry reminder sent to {} for id={}",
                     record.getSubjectEmail(),
                     record.getId());
        } catch (Exception e) {
            log.error("Failed to send expiry reminder "
                    + "for id={}: {}",
                      record.getId(), e.getMessage());
        }
    }

    // ── WITHDRAWAL CONFIRMATION ──────────────────────────────
    @Async
    @Override
    public void sendWithdrawalConfirmationEmail(
            ConsentRecord record) {
        if (mailSender == null) {
            log.warn("Mail not configured — skipping "
                   + "withdrawal confirmation for id={}",
                     record.getId());
            return;
        }
        try {
            Context ctx = new Context();
            ctx.setVariable("subjectName",
                            record.getSubjectName());
            ctx.setVariable("purpose",
                            record.getPurpose());
            ctx.setVariable("withdrawalDate",
                            record.getWithdrawalDate());

            String html = templateEngine.process(
                "email/withdrawal-confirmation", ctx);
            sendHtml(
                record.getSubjectEmail(),
                "Consent withdrawal confirmed",
                html);

            log.info("Withdrawal confirmation sent to {} "
                   + "for id={}",
                     record.getSubjectEmail(),
                     record.getId());
        } catch (Exception e) {
            log.error("Failed to send withdrawal confirmation"
                    + " for id={}: {}",
                      record.getId(), e.getMessage());
        }
    }

    // ── DAILY DIGEST ─────────────────────────────────────────
    @Async
    @Override
    public void sendDailyDigestEmail(
            String toAdmin,
            List<ConsentRecord> records) {
        if (mailSender == null) {
            log.warn("Mail not configured — skipping "
                   + "daily digest to {}",
                     toAdmin);
            return;
        }
        try {
            Context ctx = new Context();
            ctx.setVariable("records", records);
            ctx.setVariable("count",   records.size());

            String html = templateEngine.process(
                "email/daily-digest", ctx);
            sendHtml(
                toAdmin,
                "Daily digest — "
                    + records.size()
                    + " consent(s) expiring soon",
                html);

            log.info("Daily digest sent to {} "
                   + "with {} records",
                     toAdmin, records.size());
        } catch (Exception e) {
            log.error("Failed to send daily digest "
                    + "to {}: {}",
                      toAdmin, e.getMessage());
        }
    }

    // ── helper ───────────────────────────────────────────────
    private void sendHtml(String to,
                           String subject,
                           String htmlBody)
            throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper =
            new MimeMessageHelper(msg, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(msg);
    }
}