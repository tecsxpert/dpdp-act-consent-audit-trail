package com.internship.tool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.BaseIntegrationTest;
import com.internship.tool.entity.ConsentStatus;
import com.internship.tool.service.dto
    .ConsentRecordRequest;
import com.internship.tool.service.dto.LoginRequest;
import com.internship.tool.service.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation
    .Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet
    .request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet
    .result.MockMvcResultMatchers.*;

@DisplayName("AuditLog Controller Integration Tests")
class AuditLogControllerIntegrationTest
        extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setup() throws Exception {
        userToken = getToken("audituser2@test.com");
    }

    @Test
    @DisplayName("GET /audit-logs — user returns 403")
    void getAll_asUser_shouldReturn403()
            throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
            .header("Authorization",
                    "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /audit-logs — no token returns 401")
    void getAll_noToken_shouldReturn401()
            throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /audit-logs/record — user returns 403")
    void getByRecord_asUser_shouldReturn403()
            throws Exception {
        mockMvc.perform(
            get("/api/v1/audit-logs/record/ConsentRecord/1")
                .header("Authorization",
                        "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /audit-logs/performer — user returns 403")
    void getByPerformer_asUser_shouldReturn403()
            throws Exception {
        mockMvc.perform(
            get("/api/v1/audit-logs/performer")
                .param("performedBy",
                       "audituser2@test.com")
                .header("Authorization",
                        "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    // ── helper ────────────────────────────────────────────────
    private String getToken(String email)
            throws Exception {

        RegisterRequest reg = new RegisterRequest();
        reg.setFullName("Test");
        reg.setEmail(email);
        reg.setPassword("Password123");
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword("Password123");
        MvcResult r = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(login)))
            .andReturn();

        return objectMapper
            .readTree(r.getResponse()
                       .getContentAsString())
            .get("accessToken").asText();
    }
}