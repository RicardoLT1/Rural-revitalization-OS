package com.xiangyun.operation;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.BusinessException;
import com.xiangyun.common.SecurityHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system-settings")
public class SystemSettingsController {

    private final SystemSettingsService settingsService;

    public SystemSettingsController(SystemSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ApiResponse<SystemSettingsView> get(
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            @RequestHeader(value = SecurityHeaders.VILLAGE_ID, defaultValue = "1") String villageId) {
        requireStaff(role);
        return ApiResponse.success(settingsService.get(villageId));
    }

    @PutMapping
    public ApiResponse<SystemSettingsView> update(
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            @RequestHeader(value = SecurityHeaders.VILLAGE_ID, defaultValue = "1") String villageId,
            @RequestHeader(value = SecurityHeaders.USERNAME, defaultValue = "admin") String operator,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        requireAdmin(role);
        AdminAuditContext.targetId(request, villageId);
        AdminAuditContext.before(request, settingsService.get(villageId));
        SystemSettingsView result = settingsService.update(villageId, operator, body);
        AdminAuditContext.after(request, result);
        return ApiResponse.success(result);
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) throw new BusinessException(40300, "仅管理员可以维护系统设置");
    }

    private void requireStaff(String role) {
        if (!"STAFF".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(40300, "无权读取管理端系统设置");
        }
    }
}
