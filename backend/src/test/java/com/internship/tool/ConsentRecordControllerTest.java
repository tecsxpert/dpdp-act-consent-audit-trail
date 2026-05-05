package com.internship.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.config.JwtUtil;
import com.internship.tool.entity.ConsentRecord;
import com.internship.tool.service.ConsentRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ConsentRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private ConsentRecordService consentRecordService;

    private String getToken() {
        return "Bearer " + jwtUtil.generateToken("admin");
    }

    private ConsentRecord sampleRecord() {
        ConsentRecord r = new ConsentRecord();
        r.setId(1L);
        r.setDataPrincipalId("CUST-001");
        r.setDataPrincipalName("Rahul Sharma");
        r.setDataPrincipalEmail("rahul@gmail.com");
        r.setDataFiduciaryId("ORG-101");
        r.setDataFiduciaryName("HDFC Bank");
        r.setPurpose("Credit assessment");
        r.setDataCategories("Financial data");
        r.setConsentStatus("GRANTED");
        r.setIsActive(true);
        r.setIsFallback(false);
        return r;
    }

    @Test
    void getAll_shouldReturn200_withValidToken() throws Exception {
        Page<ConsentRecord> page = new PageImpl<>(List.of(sampleRecord()));
        when(consentRecordService.getAllRecords(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/consent-records")
                .header("Authorization", getToken()))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_shouldReturn403_withoutToken() throws Exception {
        mockMvc.perform(get("/api/consent-records"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_shouldReturn200_whenRecordExists() throws Exception {
        when(consentRecordService.getById(1L))
                .thenReturn(Optional.of(sampleRecord()));

        mockMvc.perform(get("/api/consent-records/1")
                .header("Authorization", getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataPrincipalName").value("Rahul Sharma"));
    }

    @Test
    void getById_shouldReturn404_whenRecordNotFound() throws Exception {
        when(consentRecordService.getById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/consent-records/99")
                .header("Authorization", getToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStats_shouldReturn200_withValidToken() throws Exception {
        when(consentRecordService.getStats())
                .thenReturn(Map.of("total", 10L, "granted", 5L,
                        "revoked", 2L, "pending", 2L, "expired", 1L));

        mockMvc.perform(get("/api/consent-records/stats")
                .header("Authorization", getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10));
    }

    @Test
    void create_shouldReturn201_withValidToken() throws Exception {
        ConsentRecord record = sampleRecord();
        when(consentRecordService.create(any(), any()))
                .thenReturn(record);

        mockMvc.perform(post("/api/consent-records")
                .header("Authorization", getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_shouldReturn403_withoutToken() throws Exception {
        mockMvc.perform(post("/api/consent-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRecord())))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_shouldReturn200_withValidToken() throws Exception {
        ConsentRecord record = sampleRecord();
        when(consentRecordService.update(anyLong(), any(), any()))
                .thenReturn(record);

        mockMvc.perform(put("/api/consent-records/1")
                .header("Authorization", getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn204_withValidToken() throws Exception {
        mockMvc.perform(delete("/api/consent-records/1")
                .header("Authorization", getToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void export_shouldReturn200_withValidToken() throws Exception {
        Page<ConsentRecord> page = new PageImpl<>(List.of(sampleRecord()));
        when(consentRecordService.getAllRecords(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/consent-records/export")
                .header("Authorization", getToken()))
                .andExpect(status().isOk());
    }
}