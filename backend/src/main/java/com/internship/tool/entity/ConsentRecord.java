package com.internship.tool.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "consent_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ConsentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // citizen giving consent
    @Column(name = "data_principal_id", nullable = false, length = 100)
    private String dataPrincipalId;

    @Column(name = "data_principal_name", nullable = false, length = 255)
    private String dataPrincipalName;

    @Column(name = "data_principal_email", nullable = false, length = 255)
    private String dataPrincipalEmail;

    // organization collecting data
    @Column(name = "data_fiduciary_id", nullable = false, length = 100)
    private String dataFiduciaryId;

    @Column(name = "data_fiduciary_name", nullable = false, length = 255)
    private String dataFiduciaryName;

    // what and why
    @Column(name = "purpose", nullable = false, length = 500)
    private String purpose;

    @Column(name = "data_categories", nullable = false, length = 500)
    private String dataCategories;

    // status — PENDING by default
    @Column(name = "consent_status", nullable = false, length = 20)
    private String consentStatus = "PENDING";

    // AI fields — filled after AI service responds
    @Column(name = "ai_description", columnDefinition = "TEXT")
    private String aiDescription;

    @Column(name = "ai_score")
    private Integer aiScore;

    @Column(name = "is_fallback")
    private Boolean isFallback = false;

    // validity dates
    @Column(name = "consent_date")
    private LocalDateTime consentDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    // soft delete
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}