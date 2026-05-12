package com.internship.tool.service;

import com.internship.tool.entity.ConsentRecord;
import com.internship.tool.repository
    .ConsentRecordRepository;
import com.internship.tool.service.dto.AiAnalysisRequest;
import com.internship.tool.service.dto.AiAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiIntegrationService {

    private final AiServiceClient         aiServiceClient;
    private final ConsentRecordRepository repository;

    // ── TRIGGER ON CREATE ────────────────────────────────────
    // Called by ConsentRecordServiceImpl after every save
    @Async
    public void triggerAnalysis(ConsentRecord record) {
        log.info("Triggering AI analysis for record id={}",
                 record.getId());

        AiAnalysisRequest request = AiAnalysisRequest
            .builder()
            .recordId(record.getId())
            .purpose(record.getPurpose())
            .dataCategories(record.getDataCategories())
            .legalBasis(record.getLegalBasis())
            .status(record.getStatus().name())
            .subjectEmail(record.getSubjectEmail())
            .build();

        AiAnalysisResponse response =
            aiServiceClient.analyse(request);
        attachResults(record.getId(), response);
    }

    // ── ATTACH RESULTS BACK TO RECORD ────────────────────────
    @Transactional
    public void attachResults(
            Long               id,
            AiAnalysisResponse response) {

        repository.findByIdAndDeletedFalse(id)
            .ifPresent(record -> {
                record.setAiDescription(
                    response.getDescription());
                record.setAiRecommendations(
                    response.getRecommendations());
                record.setAiReport(
                    response.getReport());
                record.setAiProcessed(
                    !"error".equals(response.getStatus()));
                repository.save(record);
                log.info("AI results attached to id={} "
                       + "success={}",
                         id, record.getAiProcessed());
            });
    }

    // ── HOURLY RETRY SCHEDULER ───────────────────────────────
    // Picks up any records AI has not processed yet
    @Scheduled(cron = "0 0 * * * *")
    public void retryUnprocessedRecords() {
        List<ConsentRecord> unprocessed =
            repository.findUnprocessedByAi();

        if (unprocessed.isEmpty()) {
            log.debug("AI retry scheduler: "
                    + "no unprocessed records");
            return;
        }

        log.info("AI retry scheduler: processing "
               + "{} unprocessed record(s)",
                 unprocessed.size());
        unprocessed.forEach(this::triggerAnalysis);
    }

    // ── MANUAL RETRY ─────────────────────────────────────────
    // Called from controller — admin force re-analyse
    @Async
    public void retryAnalysis(Long id) {
        repository.findByIdAndDeletedFalse(id)
            .ifPresent(record -> {
                log.info("Manual AI retry triggered "
                       + "for record id={}", id);
                record.setAiProcessed(false);
                repository.save(record);
                triggerAnalysis(record);
            });
    }
}