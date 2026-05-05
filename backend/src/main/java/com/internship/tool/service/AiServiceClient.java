package com.internship.tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.entity.ConsentRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiServiceClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String AI_BASE_URL = "http://localhost:5000";

    public void enrichWithAiDescription(ConsentRecord record,
                                         ConsentRecordService service) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("dataPrincipalName", record.getDataPrincipalName());
            body.put("dataFiduciaryName", record.getDataFiduciaryName());
            body.put("purpose", record.getPurpose());
            body.put("dataCategories", record.getDataCategories());
            body.put("consentStatus", record.getConsentStatus());

            String json = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AI_BASE_URL + "/describe"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode result = objectMapper.readTree(response.body());
                String description = result.path("description").asText();
                int score = result.path("score").asInt(50);
                boolean isFallback = result.path("isFallback").asBoolean(false);
                service.updateAiFields(record.getId(), description,
                        score, isFallback);
            }
        } catch (Exception e) {
            // AI service unavailable — use null fallback, don't crash
            System.out.println("AI service unavailable: " + e.getMessage());
            try {
                service.updateAiFields(record.getId(),
                        "AI description unavailable.", 50, true);
            } catch (Exception ignored) {}
        }
    }
}