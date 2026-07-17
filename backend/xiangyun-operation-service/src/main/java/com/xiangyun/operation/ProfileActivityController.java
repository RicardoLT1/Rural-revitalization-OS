package com.xiangyun.operation;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.BusinessException;
import com.xiangyun.common.SecurityHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileActivityController {

    private final AdminAuditService auditService;

    public ProfileActivityController(AdminAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/login-records")
    public ApiResponse<List<Map<String, Object>>> loginRecords(
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "") String userId,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            @RequestParam(defaultValue = "10") int limit) {
        if (!"STAFF".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(40300, "无权访问管理端登录记录");
        }
        return ApiResponse.success(auditService.loginRecords(userId, limit));
    }
}
