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

@DisplayName("ConsentRecord Controller Integration Tests")
class ConsentRecordControllerIntegrationTest
        extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;

    private static final String BASE =
        "/api/v1/consent-records";

    @BeforeEach
    void setupTokens() throws Exception {
        userToken  = getToken(
            "cruser@test.com",  false);
        adminToken = getToken(
            "cradmin@test.com", false);
    }

    @Test
    @DisplayName("GET / — authenticated returns 200")
    void getAll_authenticated_shouldReturn200()
            throws Exception {
        mockMvc.perform(get(BASE)
            .header("Authorization",
                    "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET / — no token returns 401")
    void getAll_noToken_shouldReturn401()
            throws Exception {
        mockMvc.perform(get(BASE))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST / — valid request returns 201")
    void create_validRequest_shouldReturn201()
            throws Exception {
        mockMvc.perform(post(BASE)
            .header("Authorization",
                    "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper
                .writeValueAsString(buildRequest())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.status")
                .value("ACTIVE"));
    }

    @Test
    @DisplayName("POST / — missing fields returns 400")
    void create_missingFields_shouldReturn400()
            throws Exception {
        mockMvc.perform(post(BASE)
            .header("Authorization",
                    "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors")
                .exists());
    }

    @Test
    @DisplayName("POST / — expiry before consent returns 400")
    void create_badDates_shouldReturn400()
            throws Exception {
        ConsentRecordRequest req = buildRequest();
        req.setConsentDate(LocalDate.of(2026, 5, 1));
        req.setExpiryDate(LocalDate.of(2025, 1, 1));

        mockMvc.perform(post(BASE)
            .header("Authorization",
                    "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper
                .writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(
                "$.fieldErrors.expiryDate").exists());
    }

    @Test
    @DisplayName("GET /{id} — existing record returns 200")
    void getById_existing_shouldReturn200()
            throws Exception {
        Long id = createAndGetId(userToken);
        mockMvc.perform(get(BASE + "/" + id)
            .header("Authorization",
                    "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("GET /{id} — nonexistent returns 404")
    void getById_nonExistent_shouldReturn404()
            throws Exception {
        mockMvc.perform(get(BASE + "/99999")
            .header("Authorization",
                    "Bearer " + userToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /{id} — valid update returns 200")
    void update_valid_shouldReturn200()
            throws Exception {
        Long id = createAndGetId(userToken);
        ConsentRecordRequest upd = buildRequest();
        upd.setStatus(ConsentStatus.WITHDRAWN);

        mockMvc.perform(put(BASE + "/" + id)
            .header("Authorization",
                    "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper
                .writeValueAsString(upd)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status")
                .value("WITHDRAWN"));
    }

    @Test
    @DisplayName("DELETE /{id} — user returns 403")
    void delete_asUser_shouldReturn403()
            throws Exception {
        Long id = createAndGetId(userToken);
        mockMvc.perform(delete(BASE + "/" + id)
            .header("Authorization",
                    "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /stats — returns KPI counts")
    void stats_shouldReturnCounts()
            throws Exception {
        mockMvc.perform(get(BASE + "/stats")
            .header("Authorization",
                    "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    @DisplayName("GET /search — returns results")
    void search_shouldReturnResults()
            throws Exception {
        createAndGetId(userToken);
        mockMvc.perform(get(BASE + "/search")
            .param("q", "Ravi")
            .header("Authorization",
                    "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    // ── helpers ──────────────────────────────────────────────

    private String getToken(
            String email,
            boolean admin) throws Exception {

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

    private Long createAndGetId(
            String token) throws Exception {
        MvcResult r = mockMvc.perform(
            post(BASE)
                .header("Authorization",
                        "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(buildRequest())))
            .andReturn();
        return objectMapper
            .readTree(r.getResponse()
                       .getContentAsString())
            .get("id").asLong();
    }

    private ConsentRecordRequest buildRequest() {
        ConsentRecordRequest req =
            new ConsentRecordRequest();
        req.setSubjectName("Ravi Kumar");
        req.setSubjectEmail("ravi@test.com");
        req.setPurpose("Marketing emails");
        req.setStatus(ConsentStatus.ACTIVE);
        req.setConsentDate(LocalDate.of(2026, 1, 1));
        req.setExpiryDate(LocalDate.of(2027, 1, 1));
        return req;
    }
}