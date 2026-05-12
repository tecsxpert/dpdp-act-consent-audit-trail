package com.internship.tool.service;

import com.internship.tool.entity.ConsentRecord;
import java.util.List;

public interface EmailService {
    void sendExpiryReminderEmail(ConsentRecord record);
    void sendWithdrawalConfirmationEmail(ConsentRecord record);
    void sendDailyDigestEmail(String toAdmin,
                               List<ConsentRecord> records);
}