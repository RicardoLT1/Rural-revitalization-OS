package com.xiangyun.operation;

import com.xiangyun.common.SecurityHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminAuditFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AdminAuditFilter.class);
    private final AdminAuditService auditService;

    public AdminAuditFilter(AdminAuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        if (HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method) || HttpMethod.OPTIONS.matches(method)) {
            return true;
        }
        String path = request.getRequestURI();
        return !isAuditedPath(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            String path = request.getRequestURI();
            AdminAuditEvent event = new AdminAuditEvent(
                    value(request, SecurityHeaders.TRACE_ID),
                    value(request, SecurityHeaders.USER_ID),
                    value(request, SecurityHeaders.USERNAME),
                    value(request, SecurityHeaders.ROLE),
                    value(request, SecurityHeaders.VILLAGE_ID),
                    module(path),
                    action(request.getMethod(), path),
                    targetType(path),
                    targetId(path),
                    request.getMethod(),
                    path,
                    clientIp(request),
                    truncate(request.getHeader("User-Agent"), 512),
                    status >= 400 ? "FAILURE" : "SUCCESS",
                    status,
                    "耗时 " + (System.currentTimeMillis() - startedAt) + "ms");
            try {
                auditService.record(event);
            } catch (Exception ex) {
                log.error("Failed to persist admin audit event: traceId={}, path={}", event.traceId(), path, ex);
            }
        }
    }

    private boolean isAuditedPath(String path) {
        if (path.startsWith("/api/resources")) {
            return true;
        }
        if (path.startsWith("/api/operation/reports/weekly") || path.startsWith("/api/todos")) {
            return true;
        }
        return path.matches("/api/workflows/[^/]+/(approve|reject|materials)")
                || path.matches("/api/workflows/processes/[^/]+/actions")
                || path.matches("/api/workflows/approvals/[^/]+/(pass|reject)");
    }

    private String module(String path) {
        if (path.startsWith("/api/resources")) return "RESOURCE";
        if (path.startsWith("/api/operation/reports")) return "REPORT";
        if (path.startsWith("/api/todos")) return "TODO";
        return "WORKFLOW";
    }

    private String action(String method, String path) {
        if (path.endsWith("/publish")) return "PUBLISH_RESOURCE";
        if (path.endsWith("/offline")) return "OFFLINE_RESOURCE";
        if (path.endsWith("/investment-status")) return "CHANGE_INVESTMENT_STATUS";
        if (path.endsWith("/approve") || path.endsWith("/pass")) return "APPROVE_WORKFLOW";
        if (path.endsWith("/reject")) return "REJECT_WORKFLOW";
        if (path.endsWith("/materials")) return "SUPPLEMENT_MATERIAL";
        if (path.endsWith("/actions")) return "PROCESS_WORKFLOW_ACTION";
        if (path.startsWith("/api/operation/reports/weekly")) return "CREATE_WEEKLY_REPORT";
        if (path.startsWith("/api/todos")) return path.endsWith("/complete") ? "COMPLETE_TODO" : "CREATE_TODO";
        if ("POST".equals(method) && "/api/resources".equals(path)) return "CREATE_RESOURCE";
        if ("PUT".equals(method)) return "UPDATE_RESOURCE";
        if ("DELETE".equals(method)) return "DELETE_RESOURCE";
        return method + "_" + module(path);
    }

    private String targetType(String path) {
        if (path.startsWith("/api/resources")) return "RESOURCE";
        if (path.startsWith("/api/operation/reports")) return "WEEKLY_REPORT";
        if (path.startsWith("/api/todos")) return "TODO";
        return "WORKFLOW";
    }

    private String targetId(String path) {
        String[] parts = path.split("/");
        if (path.startsWith("/api/resources/") && parts.length > 3) return truncate(parts[3], 128);
        if (path.startsWith("/api/workflows/processes/") && parts.length > 4) return truncate(parts[4], 128);
        if (path.startsWith("/api/workflows/approvals/") && parts.length > 4) return truncate(parts[4], 128);
        if (path.startsWith("/api/workflows/") && parts.length > 3) return truncate(parts[3], 128);
        if (path.startsWith("/api/todos/") && parts.length > 3) return truncate(parts[3], 128);
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return truncate(forwarded.split(",")[0].trim(), 64);
        }
        return truncate(request.getRemoteAddr(), 64);
    }

    private String value(HttpServletRequest request, String header) {
        return truncate(request.getHeader(header), 128);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
