package com.xiangyun.os.controller;

import com.xiangyun.os.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Auth Demo")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Operation(summary = "Login and issue demo token")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        String role = "admin".equalsIgnoreCase(request.username()) ? "platform-admin" : "village-operator";
        return ApiResponse.success(Map.of(
                "token", "demo-" + UUID.randomUUID(),
                "tokenType", "Bearer",
                "expiresIn", 7200,
                "user", Map.of(
                        "id", "u-" + request.username(),
                        "username", request.username(),
                        "displayName", "Xiangyun Demo User",
                        "role", role,
                        "permissions", List.of("dashboard:read", "resource:write", "workflow:approve", "report:read")
                )
        ));
    }

    @Operation(summary = "Get current login user")
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(Map.of(
                "authenticated", authorization != null && authorization.startsWith("Bearer "),
                "displayName", "Xiangyun Demo User",
                "role", "platform-admin",
                "villageId", "village-001",
                "loginAt", LocalDateTime.now().toString()
        ));
    }

    @Operation(summary = "Refresh demo token")
    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh() {
        return ApiResponse.success(Map.of(
                "token", "demo-" + UUID.randomUUID(),
                "expiresIn", 7200
        ));
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout() {
        return ApiResponse.success(Map.of("revoked", true));
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }
}
