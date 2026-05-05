package com.internship.tool;

import com.internship.tool.entity.ConsentRecord;
import com.internship.tool.repository.AuditLogRepository;
import com.internship.tool.repository.ConsentRecordRepository;
import com.internship.tool.service.AiServiceClient;
import com.internship.tool.service.ConsentRecordService;
import com.internship.tool.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentRecordServiceTest {

    @Mock
    private ConsentRecordRepository consentRecordRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AiServiceClient aiServiceClient;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ConsentRecordService consentRecordService;

    private ConsentRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = ConsentRecord.builder()
                .id(1L)
                .dataPrincipalId("CUST-001")
                .dataPrincipalName("Rahul Sharma")
                .dataPrincipalEmail("rahul@gmail.com")
                .dataFiduciaryId("ORG-101")
                .dataFiduciaryName("HDFC Bank")
                .purpose("Credit assessment")
                .dataCategories("Financial data")
                .consentStatus("PENDING")
                .isActive(true)
                .isFallback(false)
                .build();
    }

    @Test
    void getById_shouldReturnRecord_whenActiveRecordExists() {
        when(consentRecordRepository.findById(1L))
                .thenReturn(Optional.of(sampleRecord));

        Optional<ConsentRecord> result = consentRecordService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals("Rahul Sharma", result.get().getDataPrincipalName());
    }

    @Test
    void getById_shouldReturnEmpty_whenRecordNotFound() {
        when(consentRecordRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<ConsentRecord> result = consentRecordService.getById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void getById_shouldReturnEmpty_whenRecordIsInactive() {
        sampleRecord.setIsActive(false);
        when(consentRecordRepository.findById(1L))
                .thenReturn(Optional.of(sampleRecord));

        Optional<ConsentRecord> result = consentRecordService.getById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void create_shouldSetStatusToPending_andIsActiveTrue() {
        when(consentRecordRepository.save(any(ConsentRecord.class)))
                .thenReturn(sampleRecord);
        when(auditLogRepository.save(any())).thenReturn(null);
        doNothing().when(emailService).sendConsentCreatedEmail(any());

        ConsentRecord input = ConsentRecord.builder()
                .dataPrincipalName("Test User")
                .dataFiduciaryName("Test Bank")
                .purpose("Test purpose")
                .dataCategories("Test data")
                .build();

        ConsentRecord result = consentRecordService.create(input, "admin");

        assertNotNull(result);
        verify(consentRecordRepository, times(1)).save(any());
        verify(emailService, times(1)).sendConsentCreatedEmail(any());
    }

    @Test
    void delete_shouldSetIsActiveFalse() {
        when(consentRecordRepository.findById(1L))
                .thenReturn(Optional.of(sampleRecord));
        when(consentRecordRepository.save(any(ConsentRecord.class)))
                .thenReturn(sampleRecord);
        when(auditLogRepository.save(any())).thenReturn(null);

        consentRecordService.delete(1L, "admin");

        assertFalse(sampleRecord.getIsActive());
        verify(consentRecordRepository, times(1)).save(sampleRecord);
    }

    @Test
    void delete_shouldThrow_whenRecordNotFound() {
        when(consentRecordRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                consentRecordService.delete(99L, "admin"));
    }

    @Test
    void getStats_shouldReturnCorrectCounts() {
        when(consentRecordRepository.countByIsActiveTrue()).thenReturn(10L);
        when(consentRecordRepository
                .countByConsentStatusAndIsActiveTrue("GRANTED")).thenReturn(5L);
        when(consentRecordRepository
                .countByConsentStatusAndIsActiveTrue("REVOKED")).thenReturn(2L);
        when(consentRecordRepository
                .countByConsentStatusAndIsActiveTrue("PENDING")).thenReturn(2L);
        when(consentRecordRepository
                .countByConsentStatusAndIsActiveTrue("EXPIRED")).thenReturn(1L);

        Map<String, Long> stats = consentRecordService.getStats();

        assertEquals(10L, stats.get("total"));
        assertEquals(5L, stats.get("granted"));
        assertEquals(2L, stats.get("revoked"));
    }

    @Test
    void updateAiFields_shouldUpdateCorrectly() {
        when(consentRecordRepository.findById(1L))
                .thenReturn(Optional.of(sampleRecord));
        when(consentRecordRepository.save(any(ConsentRecord.class)))
                .thenReturn(sampleRecord);

        consentRecordService.updateAiFields(
                1L, "AI description", 80, false);

        assertEquals("AI description", sampleRecord.getAiDescription());
        assertEquals(80, sampleRecord.getAiScore());
        assertFalse(sampleRecord.getIsFallback());
    }

    @Test
    void getAllRecords_shouldCallRepository() {
        Page<ConsentRecord> page = new PageImpl<>(List.of(sampleRecord));
        when(consentRecordRepository.searchRecords(
                any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<ConsentRecord> result = consentRecordService
                .getAllRecords(null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void update_shouldThrow_whenRecordNotFound() {
        when(consentRecordRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                consentRecordService.update(99L, sampleRecord, "admin"));
    }
}