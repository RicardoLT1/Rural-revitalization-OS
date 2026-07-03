package com.xiangyun.os.controller;

import com.xiangyun.os.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Distributed Infra Demo")
@RestController
@RequestMapping("/infra")
@RequiredArgsConstructor
public class InfraController {

    private final StringRedisTemplate redisTemplate;

    @Value("${xiangyun.demo.gateway-url:http://127.0.0.1:8090}")
    private String gatewayUrl;

    @Value("${xiangyun.demo.registry-url:http://127.0.0.1:8848/nacos}")
    private String registryUrl;

    @Value("${xiangyun.demo.config-group:XIANGYUN_OS_DEMO}")
    private String configGroup;

    @Operation(summary = "Health overview")
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "service", "xiangyun-os-backend",
                "status", "UP",
                "time", LocalDateTime.now().toString()
        ));
    }

    @Operation(summary = "Redis ping")
    @GetMapping("/redis/ping")
    public ApiResponse<Map<String, Object>> redisPing() {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            return ApiResponse.success(Map.of("status", "UP", "reply", pong));
        } catch (Exception ex) {
            return ApiResponse.success(Map.of("status", "DOWN", "message", ex.getMessage()));
        }
    }

    @Operation(summary = "Write Redis demo cache")
    @PostMapping("/redis/cache/{key}")
    public ApiResponse<Map<String, Object>> putCache(@PathVariable String key, @RequestParam String value) {
        redisTemplate.opsForValue().set("xiangyun:demo:" + key, value, Duration.ofMinutes(10));
        return ApiResponse.success(Map.of("key", key, "value", value, "ttlSeconds", 600));
    }

    @Operation(summary = "Read Redis demo cache")
    @GetMapping("/redis/cache/{key}")
    public ApiResponse<Map<String, Object>> getCache(@PathVariable String key) {
        String value = redisTemplate.opsForValue().get("xiangyun:demo:" + key);
        return ApiResponse.success(Map.of("key", key, "value", value == null ? "" : value));
    }

    @Operation(summary = "Delete Redis demo cache")
    @DeleteMapping("/redis/cache/{key}")
    public ApiResponse<Map<String, Object>> deleteCache(@PathVariable String key) {
        Boolean deleted = redisTemplate.delete("xiangyun:demo:" + key);
        return ApiResponse.success(Map.of("key", key, "deleted", Boolean.TRUE.equals(deleted)));
    }

    @Operation(summary = "Service registry status")
    @GetMapping("/registry/status")
    public ApiResponse<Map<String, Object>> registryStatus() {
        return ApiResponse.success(Map.of(
                "component", "Nacos Discovery",
                "console", registryUrl,
                "registeredServices", List.of("xiangyun-gateway", "xiangyun-os-backend"),
                "demoAction", "Start Nacos, then show service online/offline in the console"
        ));
    }

    @Operation(summary = "List registered service instances")
    @GetMapping("/registry/services")
    public ApiResponse<List<Map<String, Object>>> registryServices() {
        return ApiResponse.success(List.of(
                Map.of("serviceName", "xiangyun-gateway", "host", "127.0.0.1", "port", 8090, "healthy", true),
                Map.of("serviceName", "xiangyun-os-backend", "host", "127.0.0.1", "port", 8088, "healthy", true)
        ));
    }

    @Operation(summary = "Gateway route overview")
    @GetMapping("/gateway/routes")
    public ApiResponse<List<Map<String, Object>>> gatewayRoutes() {
        return ApiResponse.success(List.of(
                Map.of("id", "backend-api", "predicate", "/api/**", "target", "lb://xiangyun-os-backend"),
                Map.of("id", "swagger-api", "predicate", "/swagger/**", "target", "lb://xiangyun-os-backend")
        ));
    }

    @Operation(summary = "Gateway access example")
    @GetMapping("/gateway/example")
    public ApiResponse<Map<String, Object>> gatewayExample() {
        return ApiResponse.success(Map.of(
                "gateway", gatewayUrl,
                "directApi", "http://127.0.0.1:8088/api/dashboard",
                "gatewayApi", gatewayUrl + "/api/dashboard",
                "description", "Use the gateway URL in the mini program when demonstrating remote service access"
        ));
    }

    @Operation(summary = "Config center current values")
    @GetMapping("/config/current")
    public ApiResponse<Map<String, Object>> configCurrent() {
        return ApiResponse.success(Map.of(
                "component", "Nacos Config",
                "group", configGroup,
                "keys", Map.of(
                        "feature.forecast.enabled", true,
                        "dashboard.cache.seconds", 300,
                        "risk.warning.threshold", 2
                )
        ));
    }

    @Operation(summary = "Config center refresh example")
    @PostMapping("/config/refresh")
    public ApiResponse<Map<String, Object>> configRefresh() {
        return ApiResponse.success(Map.of(
                "refreshed", true,
                "time", LocalDateTime.now().toString(),
                "tip", "Change configuration in Nacos, then call this endpoint in the demo"
        ));
    }
}
