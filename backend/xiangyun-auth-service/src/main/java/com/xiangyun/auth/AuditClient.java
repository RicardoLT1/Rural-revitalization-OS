package com.xiangyun.auth;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.SecurityHeaders;
import com.xiangyun.common.dto.AdminAuditRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "xiangyun-operation-service")
public interface AuditClient {

    @PostMapping("/api/internal/operation/audit-events")
    ApiResponse<Map<String, Object>> record(
            @RequestHeader(SecurityHeaders.TRACE_ID) String traceId,
            @RequestBody AdminAuditRequest request);
}
