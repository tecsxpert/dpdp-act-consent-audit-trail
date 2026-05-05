package com.internship.tool.service;

import com.internship.tool.entity.ConsentRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendConsentGrantedEmail(ConsentRecord record) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(record.getDataPrincipalEmail());
            message.setSubject("Consent Granted — " + record.getDataFiduciaryName());
            message.setText(
                "Dear " + record.getDataPrincipalName() + ",\n\n" +
                "Your consent has been granted to " + record.getDataFiduciaryName() + ".\n\n" +
                "Purpose: " + record.getPurpose() + "\n" +
                "Data Categories: " + record.getDataCategories() + "\n\n" +
                "If you did not authorize this, please contact us immediately.\n\n" +
                "DPDP Act Consent Management System"
            );
            mailSender.send(message);
            System.out.println("✅ Consent granted email sent to: " 
                + record.getDataPrincipalEmail());
        } catch (Exception e) {
            System.out.println("⚠️ Email sending failed: " + e.getMessage());
        }
    }

    @Async
    public void sendConsentRevokedEmail(ConsentRecord record) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(record.getDataPrincipalEmail());
            message.setSubject("Consent Revoked — " + record.getDataFiduciaryName());
            message.setText(
                "Dear " + record.getDataPrincipalName() + ",\n\n" +
                "Your consent has been revoked from " + record.getDataFiduciaryName() + ".\n\n" +
                "Purpose: " + record.getPurpose() + "\n\n" +
                "Your data will no longer be processed by this organization.\n\n" +
                "DPDP Act Consent Management System"
            );
            mailSender.send(message);
            System.out.println("✅ Consent revoked email sent to: " 
                + record.getDataPrincipalEmail());
        } catch (Exception e) {
            System.out.println("⚠️ Email sending failed: " + e.getMessage());
        }
    }

    @Async
    public void sendConsentCreatedEmail(ConsentRecord record) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(record.getDataPrincipalEmail());
            message.setSubject("Consent Record Created — DPDP Act");
            message.setText(
                "Dear " + record.getDataPrincipalName() + ",\n\n" +
                "A new consent record has been created for you.\n\n" +
                "Organization: " + record.getDataFiduciaryName() + "\n" +
                "Purpose: " + record.getPurpose() + "\n" +
                "Data Categories: " + record.getDataCategories() + "\n" +
                "Status: " + record.getConsentStatus() + "\n\n" +
                "DPDP Act Consent Management System"
            );
            mailSender.send(message);
            System.out.println("✅ Consent created email sent to: " 
                + record.getDataPrincipalEmail());
        } catch (Exception e) {
            System.out.println("⚠️ Email sending failed: " + e.getMessage());
        }
    }

    @Async
    public void sendExpiryReminderEmail(ConsentRecord record) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(record.getDataPrincipalEmail());
            message.setSubject("Consent Expiring Soon — " + record.getDataFiduciaryName());
            message.setText(
                "Dear " + record.getDataPrincipalName() + ",\n\n" +
                "Your consent for " + record.getDataFiduciaryName() + 
                " is expiring soon.\n\n" +
                "Purpose: " + record.getPurpose() + "\n" +
                "Expiry Date: " + record.getExpiryDate().toLocalDate() + "\n\n" +
                "Please log in to renew or revoke your consent.\n\n" +
                "DPDP Act Consent Management System"
            );
            mailSender.send(message);
            System.out.println("✅ Expiry reminder sent to: " 
                + record.getDataPrincipalEmail());
        } catch (Exception e) {
            System.out.println("⚠️ Email sending failed: " + e.getMessage());
        }
    }
}