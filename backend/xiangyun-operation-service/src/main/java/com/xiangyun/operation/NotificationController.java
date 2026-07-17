package com.xiangyun.operation;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.SecurityHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<NotificationCenterView> center(
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "") String userId,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            @RequestHeader(value = SecurityHeaders.VILLAGE_ID, defaultValue = "1") String villageId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(notificationService.center(villageId, userId, role, unreadOnly, limit));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Map<String, Object>> read(
            @PathVariable String id,
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "") String userId,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            @RequestHeader(value = SecurityHeaders.VILLAGE_ID, defaultValue = "1") String villageId) {
        return ApiResponse.success(notificationService.markRead(id, villageId, userId, role));
    }

    @PostMapping("/read-all")
    public ApiResponse<Map<String, Object>> readAll(
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "") String userId,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            @RequestHeader(value = SecurityHeaders.VILLAGE_ID, defaultValue = "1") String villageId) {
        return ApiResponse.success(notificationService.markAllRead(villageId, userId, role));
    }
}
