package com.internship.tool.repository;

import com.internship.tool.entity.ConsentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, Long> {

    Page<ConsentRecord> findByIsActiveTrue(Pageable pageable);

    @Query(value = """
        SELECT * FROM consent_record
        WHERE is_active = true
        AND (:q IS NULL OR
            data_principal_name ILIKE '%' || CAST(:q AS TEXT) || '%' OR
            data_fiduciary_name ILIKE '%' || CAST(:q AS TEXT) || '%' OR
            purpose ILIKE '%' || CAST(:q AS TEXT) || '%'
        )
        AND (:status IS NULL OR consent_status = CAST(:status AS TEXT))
        AND (CAST(:from AS TIMESTAMP) IS NULL OR created_at >= CAST(:from AS TIMESTAMP))
        AND (CAST(:to AS TIMESTAMP) IS NULL OR created_at <= CAST(:to AS TIMESTAMP))
        ORDER BY created_at DESC
        """, nativeQuery = true,
        countQuery = """
        SELECT COUNT(*) FROM consent_record
        WHERE is_active = true
        AND (:q IS NULL OR
            data_principal_name ILIKE '%' || CAST(:q AS TEXT) || '%' OR
            data_fiduciary_name ILIKE '%' || CAST(:q AS TEXT) || '%' OR
            purpose ILIKE '%' || CAST(:q AS TEXT) || '%'
        )
        AND (:status IS NULL OR consent_status = CAST(:status AS TEXT))
        AND (CAST(:from AS TIMESTAMP) IS NULL OR created_at >= CAST(:from AS TIMESTAMP))
        AND (CAST(:to AS TIMESTAMP) IS NULL OR created_at <= CAST(:to AS TIMESTAMP))
        """)
    Page<ConsentRecord> searchRecords(
        @Param("q") String q,
        @Param("status") String status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        Pageable pageable
    );

    long countByConsentStatusAndIsActiveTrue(String consentStatus);

    long countByIsActiveTrue();
}