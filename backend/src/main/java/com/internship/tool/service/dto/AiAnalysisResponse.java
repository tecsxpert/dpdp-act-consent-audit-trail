package com.internship.tool.service.dto;

import lombok.Data;

@Data
public class AiAnalysisResponse {
    private String description;
    private String recommendations;
    private String report;
    private String status;
    private String errorMessage;
}