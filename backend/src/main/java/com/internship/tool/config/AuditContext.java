package com.internship.tool.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context
    .SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request
    .RequestContextHolder;
import org.springframework.web.context.request
    .ServletRequestAttributes;

@Component
public class AuditContext {

    // Returns email of currently authenticated user
    public String currentUser() {
        Authentication auth =
            SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(
                    auth.getPrincipal())) {
            return "system";
        }
        return auth.getName();
    }

    // Returns real client IP
    // handles reverse proxy X-Forwarded-For header
    public String clientIp() {
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes)
                    RequestContextHolder
                        .getRequestAttributes();
            if (attrs == null) return "unknown";

            HttpServletRequest request =
                attrs.getRequest();
            String forwarded =
                request.getHeader("X-Forwarded-For");
            if (forwarded != null
                    && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }
}