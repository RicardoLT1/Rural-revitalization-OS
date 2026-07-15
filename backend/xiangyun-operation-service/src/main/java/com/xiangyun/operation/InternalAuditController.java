package com.xiangyun.operation;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.dto.AdminAuditRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/internal/operation/audit-events")
public class InternalAuditController {

    private final AdminAuditService auditService;

    public InternalAuditController(AdminAuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> record(@RequestBody AdminAuditRequest request) {
        auditService.record(request);
        return ApiResponse.success(Map.of("recorded", true));
    }
}
