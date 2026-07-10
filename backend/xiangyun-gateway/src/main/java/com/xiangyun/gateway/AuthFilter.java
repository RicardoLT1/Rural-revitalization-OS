package com.xiangyun.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.JwtUtils;
import com.xiangyun.common.SecurityHeaders;
import com.xiangyun.common.TokenPayload;
import com.xiangyun.common.security.InternalSignature;
import com.xiangyun.common.security.InternalSignatureSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC_PATHS = Set.of("/api/auth/login", "/api/auth/register");
    private static final Set<String> ADMIN_PREFIXES = Set.of("/api/users", "/api/roles");
    private static final Set<String> ADMIN_PATHS = Set.of("/api/dashboard/refresh");
    private static final Set<String> STAFF_OR_ADMIN_WRITE_PREFIXES = Set.of("/api/workflows/approvals");

    private final ReactiveStringRedisTemplate redisTemplate;
    private final InternalSignatureSigner internalSignatureSigner;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String secret;

    public AuthFilter(ReactiveStringRedisTemplate redisTemplate,
                      InternalSignatureSigner internalSignatureSigner,
                      @Value("${xiangyun.jwt.secret}") String secret) {
        this.redisTemplate = redisTemplate;
        this.internalSignatureSigner = internalSignatureSigner;
        this.secret = secret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String traceId = UUID.randomUUID().toString().replace("-", "");
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(withTrace(exchange, traceId));
        }
        if (path.startsWith("/api/internal/")) {
            return forbidden(exchange, "内部接口禁止外部访问");
        }
        if (PUBLIC_PATHS.contains(path)) {
            return chain.filter(withTrustedHeaders(exchange, traceId, null));
        }
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "未登录");
        }
        TokenPayload payload;
        try {
            payload = JwtUtils.parse(authorization.substring(7), secret);
        } catch (Exception ex) {
            return unauthorized(exchange, ex.getMessage());
        }
        if ("/api/auth/logout".equals(path)) {
            return logout(exchange, payload);
        }
        return redisTemplate.opsForValue().get("login:token:" + payload.jti())
                .flatMap(value -> {
                    if (!isAuthorized(path, exchange.getRequest().getMethod().name(), payload.role())) {
                        return forbidden(exchange, "无权访问该功能");
                    }
                    return chain.filter(withTrustedHeaders(exchange, traceId, payload));
                })
                .switchIfEmpty(unauthorized(exchange, "登录会话已失效"));
    }

    private boolean isAuthorized(String path, String method, String role) {
        if (requiresAdmin(path)) {
            return "ADMIN".equals(role);
        }
        if (requiresStaffOrAdmin(path, method)) {
            return "STAFF".equals(role) || "ADMIN".equals(role);
        }
        return true;
    }

    private boolean requiresAdmin(String path) {
        if (ADMIN_PATHS.contains(path)) {
            return true;
        }
        return ADMIN_PREFIXES.stream().anyMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"));
    }

    private boolean requiresStaffOrAdmin(String path, String method) {
        if (!"POST".equals(method) && !"PUT".equals(method) && !"DELETE".equals(method)) {
            return false;
        }
        if (STAFF_OR_ADMIN_WRITE_PREFIXES.stream().anyMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"))) {
            return true;
        }
        return path.startsWith("/api/workflows/processes/") && path.endsWith("/actions");
    }

    private ServerWebExchange withTrace(ServerWebExchange exchange, String traceId) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    removeTrustedHeaders(headers);
                    headers.set(SecurityHeaders.TRACE_ID, traceId);
                })
                .build();
        return exchange.mutate().request(request).build();
    }

    private ServerWebExchange withTrustedHeaders(ServerWebExchange exchange, String traceId, TokenPayload payload) {
        InternalSignature signature = internalSignatureSigner.sign(
                exchange.getRequest().getMethod().name(),
                pathWithQuery(exchange),
                traceId,
                "");
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    removeTrustedHeaders(headers);
                    headers.set(SecurityHeaders.TRACE_ID, traceId);
                    headers.set(SecurityHeaders.INTERNAL_SERVICE, signature.serviceName());
                    headers.set(SecurityHeaders.INTERNAL_TIMESTAMP, signature.timestamp());
                    headers.set(SecurityHeaders.INTERNAL_NONCE, signature.nonce());
                    headers.set(SecurityHeaders.INTERNAL_SIGNATURE, signature.signature());
                    if (payload != null) {
                        headers.set(SecurityHeaders.USER_ID, payload.userId());
                        headers.set(SecurityHeaders.USERNAME, payload.username());
                        headers.set(SecurityHeaders.ROLE, payload.role());
                        headers.set(SecurityHeaders.VILLAGE_ID, payload.villageId());
                    }
                })
                .build();
        return exchange.mutate().request(request).build();
    }

    private void removeTrustedHeaders(org.springframework.http.HttpHeaders headers) {
        headers.remove(SecurityHeaders.USER_ID);
        headers.remove(SecurityHeaders.USERNAME);
        headers.remove(SecurityHeaders.ROLE);
        headers.remove(SecurityHeaders.VILLAGE_ID);
        headers.remove(SecurityHeaders.INTERNAL_SERVICE);
        headers.remove(SecurityHeaders.INTERNAL_TIMESTAMP);
        headers.remove(SecurityHeaders.INTERNAL_NONCE);
        headers.remove(SecurityHeaders.INTERNAL_SIGNATURE);
    }

    private String pathWithQuery(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getRawPath();
        String query = exchange.getRequest().getURI().getRawQuery();
        return query == null || query.isBlank() ? path : path + "?" + query;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return writeError(exchange, HttpStatus.UNAUTHORIZED, 40100, message);
    }

    private Mono<Void> logout(ServerWebExchange exchange, TokenPayload payload) {
        return Mono.when(
                        redisTemplate.delete("login:token:" + payload.jti()),
                        redisTemplate.delete("auth:session:" + payload.jti()),
                        redisTemplate.opsForSet().remove("auth:user:sessions:" + payload.userId(), payload.jti())
                )
                .then(writeJson(exchange, HttpStatus.OK, ApiResponse.success(Map.of("logout", true))));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        return writeError(exchange, HttpStatus.FORBIDDEN, 40300, message);
    }

    private Mono<Void> writeJson(ServerWebExchange exchange, HttpStatus status, Object body) {
        try {
            byte[] bytes = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            exchange.getResponse().getHeaders().setContentLength(bytes.length);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception ex) {
            return exchange.getResponse().setComplete();
        }
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, int code, String message) {
        return writeJson(exchange, status, ApiResponse.fail(code, message));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
