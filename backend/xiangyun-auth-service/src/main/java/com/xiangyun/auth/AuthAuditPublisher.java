package com.xiangyun.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.SecurityHeaders;
import com.xiangyun.common.dto.AdminAuditRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthAuditPublisher {
    private static final Logger log = LoggerFactory.getLogger(AuthAuditPublisher.class);

    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;

    public AuthAuditPublisher(AuditClient auditClient, ObjectMapper objectMapper) {
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
    }

    public void record(HttpServletRequest request,
                       String action,
                       String targetType,
                       String targetId,
                       String result,
                       int httpStatus,
                       String detail,
                       Object beforeData,
                       Object afterData) {
        String traceId = header(request, SecurityHeaders.TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        AdminAuditRequest event = new AdminAuditRequest(
                traceId,
                header(request, SecurityHeaders.USER_ID),
                value(header(request, SecurityHeaders.USERNAME), "system"),
                value(header(request, SecurityHeaders.ROLE), "SYSTEM"),
                header(request, SecurityHeaders.VILLAGE_ID),
                "USER",
                action,
                targetType,
                targetId,
                request.getMethod(),
                pathWithQuery(request),
                clientIp(request),
                request.getHeader("User-Agent"),
                result,
                httpStatus,
                detail,
                json(beforeData),
                json(afterData));
        publish(traceId, event);
    }

    public void recordSecurity(HttpServletRequest request,
                               String action,
                               String actorId,
                               String actorName,
                               String actorRole,
                               String villageId,
                               String targetId,
                               String result,
                               int httpStatus,
                               String detail) {
        String traceId = header(request, SecurityHeaders.TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        AdminAuditRequest event = new AdminAuditRequest(
                traceId,
                actorId,
                value(actorName, "anonymous"),
                value(actorRole, "ANONYMOUS"),
                villageId,
                "SECURITY",
                action,
                "SESSION",
                targetId,
                request.getMethod(),
                pathWithQuery(request),
                clientIp(request),
                request.getHeader("User-Agent"),
                result,
                httpStatus,
                detail,
                null,
                null);
        publish(traceId, event);
    }

    private void publish(String traceId, AdminAuditRequest event) {
        try {
            auditClient.record(traceId, event);
        } catch (Exception ex) {
            log.warn("Failed to persist auth audit: traceId={}, action={}, targetId={}",
                    traceId, event.action(), event.targetId());
        }
    }

    private String json(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }

    private String pathWithQuery(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null || query.isBlank() ? request.getRequestURI() : request.getRequestURI() + "?" + query;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private String header(HttpServletRequest request, String name) {
        return request.getHeader(name);
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
