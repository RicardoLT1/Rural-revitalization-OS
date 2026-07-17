package com.xiangyun.auth;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.BusinessException;
import com.xiangyun.common.dto.LoginRequest;
import com.xiangyun.common.dto.LoginResponse;
import com.xiangyun.common.dto.PageResponse;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final AuthAuditPublisher auditPublisher;

    public AuthController(AuthService authService, AuthAuditPublisher auditPublisher) {
        this.authService = authService;
        this.auditPublisher = auditPublisher;
    }

    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest servletRequest) {
        try {
            LoginResponse response = authService.login(request.username(), request.password());
            LoginResponse.UserProfile user = response.user();
            auditPublisher.recordSecurity(servletRequest, "LOGIN_SUCCESS", user.id(), user.username(),
                    user.role(), user.villageId(), user.id(), "SUCCESS", 200, "登录成功");
            return ApiResponse.success(response);
        } catch (RuntimeException ex) {
            int status = statusOf(ex);
            auditPublisher.recordSecurity(servletRequest, "LOGIN_FAILURE", null, request.username(),
                    "ANONYMOUS", null, request.username(), "FAILURE", status, ex.getMessage());
            throw ex;
        }
    }

    @PostMapping("/auth/register")
    public ApiResponse<LoginResponse> register(@RequestBody Map<String, Object> body) {
        String username = String.valueOf(body.getOrDefault("username", ""));
        String password = String.valueOf(body.getOrDefault("password", ""));
        String displayName = String.valueOf(body.getOrDefault("displayName", username));
        return ApiResponse.success(authService.register(username, password, displayName));
    }

    @GetMapping("/auth/me")
    public ApiResponse<LoginResponse.UserProfile> me(@RequestHeader("Authorization") String authorization) {
        return ApiResponse.success(authService.me(authorization));
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Map<String, Object>> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return ApiResponse.success(Map.of("logout", true));
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<Map<String, Object>>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ApiResponse.success(authService.userPage(keyword, role, enabled, page, pageSize));
    }

    @GetMapping("/internal/users/search")
    public ApiResponse<List<com.xiangyun.common.dto.UserSummary>> searchUsers(
            @RequestParam String keyword,
            @RequestParam String villageId,
            @RequestParam(defaultValue = "5") Integer limit) {
        return ApiResponse.success(authService.searchUserSummaries(keyword, villageId, limit));
    }

    @PostMapping("/users")
    public ApiResponse<Map<String, Object>> createUser(@RequestBody Map<String, Object> body,
                                                       HttpServletRequest request) {
        return ApiResponse.success(audited(request, "CREATE_USER", "USER", null, () -> null,
                () -> authService.createUser(body),
                result -> authService.user(String.valueOf(result.get("id")))));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> updateUser(@PathVariable String id,
                                                       @RequestBody Map<String, Object> body,
                                                       HttpServletRequest request) {
        return ApiResponse.success(audited(request, "UPDATE_USER", "USER", id, () -> authService.user(id),
                () -> authService.updateUser(id, body), result -> result));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> deleteUser(@PathVariable String id, HttpServletRequest request) {
        Map<String, Object> result = audited(request, "DELETE_USER", "USER", id, () -> authService.user(id),
                () -> authService.enableUser(id, false), value -> value);
        return ApiResponse.success(Map.of("id", id, "deleted", true, "enabled", result.get("enabled")));
    }

    @PostMapping("/users/{id}/enable")
    public ApiResponse<Map<String, Object>> enableUser(@PathVariable String id, HttpServletRequest request) {
        return ApiResponse.success(audited(request, "ENABLE_USER", "USER", id, () -> authService.user(id),
                () -> authService.enableUser(id, true), result -> result));
    }

    @PostMapping("/users/{id}/disable")
    public ApiResponse<Map<String, Object>> disableUser(@PathVariable String id, HttpServletRequest request) {
        return ApiResponse.success(audited(request, "DISABLE_USER", "USER", id, () -> authService.user(id),
                () -> authService.enableUser(id, false), result -> result));
    }

    @PostMapping("/users/{id}/password")
    public ApiResponse<Map<String, Object>> changePassword(@PathVariable String id,
                                                           @RequestBody Map<String, Object> body,
                                                           HttpServletRequest request) {
        String password = String.valueOf(body.getOrDefault("password", "123456"));
        return ApiResponse.success(audited(request, "RESET_USER_PASSWORD", "USER", id,
                () -> Map.of("passwordChanged", false),
                () -> authService.resetPassword(id, password), result -> result));
    }

    @GetMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> roles() {
        return ApiResponse.success(authService.roles());
    }

    @PostMapping("/roles")
    public ApiResponse<Map<String, Object>> createRole(@RequestBody Map<String, Object> body,
                                                       HttpServletRequest request) {
        return ApiResponse.success(audited(request, "CREATE_ROLE", "ROLE", String.valueOf(body.getOrDefault("code", "new")),
                () -> null, () -> Map.of("created", true, "payload", body), result -> result));
    }

    @PutMapping("/roles/{code}")
    public ApiResponse<Map<String, Object>> updateRole(@PathVariable String code,
                                                       @RequestBody Map<String, Object> body,
                                                       HttpServletRequest request) {
        return ApiResponse.success(audited(request, "UPDATE_ROLE", "ROLE", code,
                () -> Map.of("code", code),
                () -> Map.of("code", code, "updated", true, "payload", body), result -> result));
    }

    @PostMapping("/users/{id}/roles/{role}")
    public ApiResponse<Map<String, Object>> assignRole(@PathVariable String id,
                                                       @PathVariable String role,
                                                       HttpServletRequest request) {
        return ApiResponse.success(audited(request, "ASSIGN_USER_ROLE", "USER", id, () -> authService.user(id),
                () -> authService.assignRole(id, role), result -> result));
    }

    @GetMapping("/internal/users/{id}/summary")
    public ApiResponse<com.xiangyun.common.dto.UserSummary> userSummary(@PathVariable String id) {
        return ApiResponse.success(authService.summary(id));
    }

    private Map<String, Object> audited(HttpServletRequest request,
                                        String action,
                                        String targetType,
                                        String targetId,
                                        Supplier<Object> beforeSupplier,
                                        Supplier<Map<String, Object>> operation,
                                        Function<Map<String, Object>, Object> afterResolver) {
        Object before = null;
        try {
            before = beforeSupplier.get();
            Map<String, Object> result = operation.get();
            String actualTargetId = targetId == null ? String.valueOf(result.getOrDefault("id", targetType)) : targetId;
            auditPublisher.record(request, action, targetType, actualTargetId, "SUCCESS", 200,
                    "管理员操作完成", before, afterResolver.apply(result));
            return result;
        } catch (RuntimeException ex) {
            int status = statusOf(ex);
            auditPublisher.record(request, action, targetType, targetId, "FAILURE", status,
                    ex.getMessage(), before, null);
            throw ex;
        }
    }

    private int statusOf(RuntimeException ex) {
        return ex instanceof BusinessException business && business.getCode() != null
                ? business.getCode() / 100
                : 500;
    }
}
