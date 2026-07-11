package com.xiangyun.gateway;

import com.xiangyun.common.JwtUtils;
import com.xiangyun.common.SecurityHeaders;
import com.xiangyun.common.TokenPayload;
import com.xiangyun.common.security.InternalAuthProperties;
import com.xiangyun.common.security.InternalSignatureSigner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthFilterTest {

    private ReactiveStringRedisTemplate redisTemplate;
    private ReactiveValueOperations<String, String> valueOperations;
    private ReactiveSetOperations<String, String> setOperations;
    private AuthFilter filter;
    private InternalAuthProperties internalAuthProperties;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(ReactiveStringRedisTemplate.class);
        valueOperations = mock(ReactiveValueOperations.class);
        setOperations = mock(ReactiveSetOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(redisTemplate.delete(anyString())).thenReturn(Mono.just(1L));
        when(setOperations.remove(anyString(), anyString())).thenReturn(Mono.just(1L));
        internalAuthProperties = new InternalAuthProperties();
        internalAuthProperties.setServiceName("xiangyun-gateway");
        internalAuthProperties.setSecret("internal-secret");
        filter = new AuthFilter(redisTemplate, new InternalSignatureSigner(internalAuthProperties), "gateway-secret");
    }

    @Test
    void loginPathIsPublic() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/auth/login"));
        AtomicBoolean called = new AtomicBoolean(false);
        GatewayFilterChain chain = next -> {
            called.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(called).isTrue();
    }

    @Test
    void registerPathIsPublic() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/auth/register"));
        AtomicBoolean called = new AtomicBoolean(false);

        filter.filter(exchange, next -> {
            called.set(true);
            return Mono.empty();
        }).block();

        assertThat(called).isTrue();
    }

    @Test
    void optionsPreflightIsPublic() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.options("/api/dashboard"));
        AtomicBoolean called = new AtomicBoolean(false);

        filter.filter(exchange, next -> {
            called.set(true);
            return Mono.empty();
        }).block();

        assertThat(called).isTrue();
    }

    @Test
    void requestWithoutTokenIsRejected() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/dashboard"));

        filter.filter(exchange, next -> Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void validTokenWritesTrustedHeaders() {
        TokenPayload token = JwtUtils.create("1", "admin", "ADMIN", "1", 3600, "gateway-secret");
        when(valueOperations.get(anyString())).thenReturn(Mono.just("1"));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.token()));
        AtomicReference<String> role = new AtomicReference<>();
        AtomicReference<String> traceId = new AtomicReference<>();
        AtomicReference<String> internalService = new AtomicReference<>();
        AtomicReference<String> internalSignature = new AtomicReference<>();
        GatewayFilterChain chain = next -> {
            role.set(next.getRequest().getHeaders().getFirst(SecurityHeaders.ROLE));
            traceId.set(next.getRequest().getHeaders().getFirst(SecurityHeaders.TRACE_ID));
            internalService.set(next.getRequest().getHeaders().getFirst(SecurityHeaders.INTERNAL_SERVICE));
            internalSignature.set(next.getRequest().getHeaders().getFirst(SecurityHeaders.INTERNAL_SIGNATURE));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(role.get()).isEqualTo("ADMIN");
        assertThat(traceId.get()).isNotBlank();
        assertThat(internalService.get()).isEqualTo("xiangyun-gateway");
        assertThat(internalSignature.get()).isNotBlank();
    }

    @Test
    void validTokenOverridesSpoofedUserHeaders() {
        TokenPayload token = JwtUtils.create("2", "staff_demo", "STAFF", "1", 3600, "gateway-secret");
        when(valueOperations.get(anyString())).thenReturn(Mono.just("2"));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.token())
                .header(SecurityHeaders.USER_ID, "3")
                .header(SecurityHeaders.USERNAME, "admin")
                .header(SecurityHeaders.ROLE, "ADMIN")
                .header(SecurityHeaders.INTERNAL_SERVICE, "evil-client")
                .header(SecurityHeaders.INTERNAL_TIMESTAMP, "1")
                .header(SecurityHeaders.INTERNAL_NONCE, "nonce")
                .header(SecurityHeaders.INTERNAL_SIGNATURE, "bad-signature"));
        AtomicReference<String> userId = new AtomicReference<>();
        AtomicReference<String> username = new AtomicReference<>();
        AtomicReference<String> role = new AtomicReference<>();
        AtomicReference<String> internalService = new AtomicReference<>();
        GatewayFilterChain chain = next -> {
            userId.set(next.getRequest().getHeaders().getFirst(SecurityHeaders.USER_ID));
            username.set(next.getRequest().getHeaders().getFirst(SecurityHeaders.USERNAME));
            role.set(next.getRequest().getHeaders().getFirst(SecurityHeaders.ROLE));
            internalService.set(next.getRequest().getHeaders().getFirst(SecurityHeaders.INTERNAL_SERVICE));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(userId.get()).isEqualTo("2");
        assertThat(username.get()).isEqualTo("staff_demo");
        assertThat(role.get()).isEqualTo("STAFF");
        assertThat(internalService.get()).isEqualTo("xiangyun-gateway");
    }

    @Test
    void missingRedisSessionIsRejected() {
        TokenPayload token = JwtUtils.create("1", "admin", "ADMIN", "1", 3600, "gateway-secret");
        when(valueOperations.get(anyString())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.token()));

        filter.filter(exchange, next -> Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logoutInvalidatesSessionAtGateway() {
        TokenPayload token = JwtUtils.create("2", "staff_demo", "STAFF", "1", 3600, "gateway-secret");
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.token()));

        filter.filter(exchange, next -> Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("\"logout\":true");
    }

    @Test
    void unauthorizedResponseUsesFriendlyMessage() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/dashboard"));

        filter.filter(exchange, next -> Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("未登录");
    }

    @Test
    void userCannotCallApprovalWriteApi() {
        TokenPayload token = JwtUtils.create("1", "user_demo", "USER", "1", 3600, "gateway-secret");
        when(valueOperations.get(anyString())).thenReturn(Mono.just("1"));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/workflows/processes/201/actions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.token()));

        filter.filter(exchange, next -> Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void staffCanCallApprovalWriteApi() {
        TokenPayload token = JwtUtils.create("2", "staff_demo", "STAFF", "1", 3600, "gateway-secret");
        when(valueOperations.get(anyString())).thenReturn(Mono.just("2"));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/workflows/processes/201/actions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.token()));
        AtomicBoolean called = new AtomicBoolean(false);

        filter.filter(exchange, next -> {
            called.set(true);
            return Mono.empty();
        }).block();

        assertThat(called).isTrue();
    }

    @Test
    void staffCannotCallUserManagementApi() {
        TokenPayload token = JwtUtils.create("2", "staff_demo", "STAFF", "1", 3600, "gateway-secret");
        when(valueOperations.get(anyString())).thenReturn(Mono.just("2"));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.token()));

        filter.filter(exchange, next -> Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void staffCannotRefreshDashboardCache() {
        TokenPayload token = JwtUtils.create("2", "staff_demo", "STAFF", "1", 3600, "gateway-secret");
        when(valueOperations.get(anyString())).thenReturn(Mono.just("2"));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/dashboard/refresh")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.token()));

        filter.filter(exchange, next -> Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminCanRefreshDashboardCache() {
        TokenPayload token = JwtUtils.create("3", "admin", "ADMIN", "1", 3600, "gateway-secret");
        when(valueOperations.get(anyString())).thenReturn(Mono.just("3"));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/dashboard/refresh")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.token()));
        AtomicBoolean called = new AtomicBoolean(false);

        filter.filter(exchange, next -> {
            called.set(true);
            return Mono.empty();
        }).block();

        assertThat(called).isTrue();
    }

    @Test
    void internalApiIsNotPubliclyAccessible() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/internal/operation/stats"));

        filter.filter(exchange, next -> Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminCanCallUserManagementApi() {
        TokenPayload token = JwtUtils.create("3", "admin", "ADMIN", "1", 3600, "gateway-secret");
        when(valueOperations.get(anyString())).thenReturn(Mono.just("3"));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.token()));
        AtomicBoolean called = new AtomicBoolean(false);

        filter.filter(exchange, next -> {
            called.set(true);
            return Mono.empty();
        }).block();

        assertThat(called).isTrue();
    }
}
