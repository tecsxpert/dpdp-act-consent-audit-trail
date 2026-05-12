package com.internship.tool.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiAnalysisRequest {
    private Long   recordId;
    private String purpose;
    private String dataCategories;
    private String legalBasis;
    private String status;
    private String subjectEmail;
}