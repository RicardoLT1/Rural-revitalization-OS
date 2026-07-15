package com.xiangyun.auth;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.dto.LoginRequest;
import com.xiangyun.common.dto.LoginResponse;
import com.xiangyun.common.dto.PageResponse;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request.username(), request.password()));
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

    @PostMapping("/users")
    public ApiResponse<Map<String, Object>> createUser(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(authService.createUser(body));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> updateUser(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(authService.updateUser(id, body));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> deleteUser(@PathVariable String id) {
        Map<String, Object> result = authService.enableUser(id, false);
        return ApiResponse.success(Map.of("id", id, "deleted", true, "enabled", result.get("enabled")));
    }

    @PostMapping("/users/{id}/enable")
    public ApiResponse<Map<String, Object>> enableUser(@PathVariable String id) {
        return ApiResponse.success(authService.enableUser(id, true));
    }

    @PostMapping("/users/{id}/disable")
    public ApiResponse<Map<String, Object>> disableUser(@PathVariable String id) {
        return ApiResponse.success(authService.enableUser(id, false));
    }

    @PostMapping("/users/{id}/password")
    public ApiResponse<Map<String, Object>> changePassword(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String password = String.valueOf(body.getOrDefault("password", "123456"));
        return ApiResponse.success(authService.resetPassword(id, password));
    }

    @GetMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> roles() {
        return ApiResponse.success(authService.roles());
    }

    @PostMapping("/roles")
    public ApiResponse<Map<String, Object>> createRole(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("created", true, "payload", body));
    }

    @PutMapping("/roles/{code}")
    public ApiResponse<Map<String, Object>> updateRole(@PathVariable String code, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("code", code, "updated", true, "payload", body));
    }

    @PostMapping("/users/{id}/roles/{role}")
    public ApiResponse<Map<String, Object>> assignRole(@PathVariable String id, @PathVariable String role) {
        return ApiResponse.success(authService.assignRole(id, role));
    }

    @GetMapping("/internal/users/{id}/summary")
    public ApiResponse<com.xiangyun.common.dto.UserSummary> userSummary(@PathVariable String id) {
        return ApiResponse.success(authService.summary(id));
    }
}
