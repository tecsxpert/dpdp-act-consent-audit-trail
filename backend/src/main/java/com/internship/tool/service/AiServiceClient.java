package com.internship.tool.service;

import com.internship.tool.exception.AiServiceException;
import com.internship.tool.service.dto.AiAnalysisRequest;
import com.internship.tool.service.dto.AiAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceClient {

    private final RestTemplate aiRestTemplate;

    @Value("${ai-service.base-url}")
    private String aiBaseUrl;

    private static final String ANALYSE_PATH =
        "/api/analyse";

    // ── CALL WITH RETRY ──────────────────────────────────────
    // attempt 1 → immediate
    // attempt 2 → 2 seconds later
    // attempt 3 → 4 seconds later
    // all fail   → recover() called
    @Retryable(
        retryFor    = {
            AiServiceException.class,
            ResourceAccessException.class
        },
        maxAttempts = 3,
        backoff     = @Backoff(
            delay      = 2000,
            multiplier = 2.0)
    )
    public AiAnalysisResponse analyse(
            AiAnalysisRequest request) {

        log.info("Calling AI service for record id={}",
                 request.getRecordId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AiAnalysisRequest> entity =
            new HttpEntity<>(request, headers);

        try {
            ResponseEntity<AiAnalysisResponse> response =
                aiRestTemplate.exchange(
                    aiBaseUrl + ANALYSE_PATH,
                    HttpMethod.POST,
                    entity,
                    AiAnalysisResponse.class);

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {
                log.info("AI service responded OK "
                       + "for record id={}",
                         request.getRecordId());
                return response.getBody();
            }

            throw new AiServiceException(
                "AI service returned status: "
                + response.getStatusCode());

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI service call failed "
                   + "(will retry): {}",
                     e.getMessage());
            throw new AiServiceException(
                "AI service unreachable: "
                + e.getMessage(), e);
        }
    }

    // ── FALLBACK — called after all 3 retries fail ───────────
    @Recover
    public AiAnalysisResponse recover(
            AiServiceException ex,
            AiAnalysisRequest  request) {

        log.error("AI service failed after 3 retries "
                + "for record id={}: {}",
                  request.getRecordId(),
                  ex.getMessage());

        AiAnalysisResponse fallback =
            new AiAnalysisResponse();
        fallback.setStatus("error");
        fallback.setErrorMessage(
            "AI service unavailable after retries: "
            + ex.getMessage());
        fallback.setDescription(
            "AI analysis pending — "
            + "service temporarily unavailable.");
        fallback.setRecommendations(
            "Please retry AI analysis manually.");
        fallback.setReport("No report generated.");
        return fallback;
    }

    @Recover
    public AiAnalysisResponse recover(
            ResourceAccessException ex,
            AiAnalysisRequest       request) {

        log.error("AI service timeout for record id={}: {}",
                  request.getRecordId(), ex.getMessage());

        AiAnalysisResponse fallback =
            new AiAnalysisResponse();
        fallback.setStatus("error");
        fallback.setErrorMessage(
            "AI service timed out: " + ex.getMessage());
        fallback.setDescription(
            "AI analysis pending — connection timed out.");
        fallback.setRecommendations(
            "Please retry AI analysis manually.");
        fallback.setReport("No report generated.");
        return fallback;
    }
}