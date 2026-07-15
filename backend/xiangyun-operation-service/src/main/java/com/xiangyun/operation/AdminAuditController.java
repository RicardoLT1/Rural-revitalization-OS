package com.xiangyun.operation;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.BusinessException;
import com.xiangyun.common.SecurityHeaders;
import com.xiangyun.common.dto.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
public class AdminAuditController {

    private final AdminAuditService auditService;

    public AdminAuditController(AdminAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Map<String, Object>>> page(
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String result,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(40301, "仅管理员可查看审计日志");
        }
        return ApiResponse.success(auditService.page(keyword, module, result, page, pageSize));
    }
}
