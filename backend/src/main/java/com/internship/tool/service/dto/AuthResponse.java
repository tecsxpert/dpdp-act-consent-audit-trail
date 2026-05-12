package com.internship.tool.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String      accessToken;
    private String      refreshToken;
    private String      tokenType;
    private Long        expiresIn;
    private String      email;
    private String      fullName;
    private Set<String> roles;
}