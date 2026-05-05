package com.internship.tool.controller;

import com.internship.tool.entity.ConsentRecord;
import com.internship.tool.service.ConsentRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/consent-records")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConsentRecordController {

    private final ConsentRecordService consentRecordService;

    // GET all records with search and filters
    @GetMapping
    public ResponseEntity<Page<ConsentRecord>> getAllRecords(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                consentRecordService.getAllRecords(q, status, from, to, page, size));
    }

    // GET single record by id
    @GetMapping("/{id}")
    public ResponseEntity<ConsentRecord> getById(@PathVariable Long id) {
        return consentRecordService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET dashboard stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(consentRecordService.getStats());
    }

    // POST create new record
    @PostMapping
    public ResponseEntity<ConsentRecord> create(
            @Valid @RequestBody ConsentRecord record,
            Authentication auth) {

        String performedBy = auth != null ? auth.getName() : "system";
        ConsentRecord saved = consentRecordService.create(record, performedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT update existing record
    @PutMapping("/{id}")
    public ResponseEntity<ConsentRecord> update(
            @PathVariable Long id,
            @Valid @RequestBody ConsentRecord record,
            Authentication auth) {

        String performedBy = auth != null ? auth.getName() : "system";
        return ResponseEntity.ok(
                consentRecordService.update(id, record, performedBy));
    }

    // DELETE soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication auth) {

        String performedBy = auth != null ? auth.getName() : "system";
        consentRecordService.delete(id, performedBy);
        return ResponseEntity.noContent().build();
    }

    // GET export CSV
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Principal Name,Principal Email,Fiduciary Name,")
           .append("Purpose,Data Categories,Status,Consent Date,")
           .append("Expiry Date,AI Score,Created At\n");

        consentRecordService.getAllRecords(null, null, null, null, 0, 1000)
                .getContent()
                .forEach(r -> csv.append(r.getId()).append(",")
                        .append(r.getDataPrincipalName()).append(",")
                        .append(r.getDataPrincipalEmail()).append(",")
                        .append(r.getDataFiduciaryName()).append(",")
                        .append(r.getPurpose()).append(",")
                        .append(r.getDataCategories()).append(",")
                        .append(r.getConsentStatus()).append(",")
                        .append(r.getConsentDate() != null
                                ? r.getConsentDate().toString() : "").append(",")
                        .append(r.getExpiryDate() != null
                                ? r.getExpiryDate().toString() : "").append(",")
                        .append(r.getAiScore() != null
                                ? r.getAiScore() : "").append(",")
                        .append(r.getCreatedAt()).append("\n"));

        byte[] bytes = csv.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment",
                "consent-records.csv");

        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}