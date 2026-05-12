package com.internship.tool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.BaseIntegrationTest;
import com.internship.tool.service.dto.LoginRequest;
import com.internship.tool.service.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation
    .Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet
    .request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet
    .request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet
    .result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet
    .result.MockMvcResultMatchers.status;

@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest
        extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String EMAIL =
        "testauth@test.com";
    private static final String PASSWORD =
        "Password123";

    @Test
    @DisplayName("POST /register — returns 201 with tokens")
    void register_shouldReturn201() throws Exception {

        RegisterRequest request =
            new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken")
                .isNotEmpty())
            .andExpect(jsonPath("$.email")
                .value(EMAIL));
    }

    @Test
    @DisplayName("POST /register — duplicate email returns 409")
    void register_duplicateEmail_shouldReturn409()
            throws Exception {

        RegisterRequest request =
            new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("dup@test.com");
        request.setPassword(PASSWORD);

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /register — invalid email returns 400")
    void register_invalidEmail_shouldReturn400()
            throws Exception {

        RegisterRequest request =
            new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("not-an-email");
        request.setPassword(PASSWORD);

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(
                "$.fieldErrors.email").exists());
    }

    @Test
    @DisplayName("POST /login — valid credentials return 200")
    void login_validCredentials_shouldReturn200()
            throws Exception {

        RegisterRequest reg = new RegisterRequest();
        reg.setFullName("Login Test");
        reg.setEmail("logintest2@test.com");
        reg.setPassword(PASSWORD);
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setEmail("logintest2@test.com");
        login.setPassword(PASSWORD);

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken")
                .isNotEmpty());
    }

    @Test
    @DisplayName("POST /login — wrong password returns 401")
    void login_wrongPassword_shouldReturn401()
            throws Exception {

        LoginRequest login = new LoginRequest();
        login.setEmail("nobody@test.com");
        login.setPassword("WrongPassword1");

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(login)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /me — with valid token returns 200")
    void me_withValidToken_shouldReturn200()
            throws Exception {

        RegisterRequest reg = new RegisterRequest();
        reg.setFullName("Me Test");
        reg.setEmail("metest2@test.com");
        reg.setPassword(PASSWORD);

        MvcResult result = mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(reg)))
            .andReturn();

        String token = objectMapper
            .readTree(result.getResponse()
                            .getContentAsString())
            .get("accessToken").asText();

        mockMvc.perform(get("/api/v1/auth/me")
            .header("Authorization",
                    "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email")
                .value("metest2@test.com"));
    }

    @Test
    @DisplayName("GET /me — without token returns 401")
    void me_withoutToken_shouldReturn401()
            throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isUnauthorized());
    }
}