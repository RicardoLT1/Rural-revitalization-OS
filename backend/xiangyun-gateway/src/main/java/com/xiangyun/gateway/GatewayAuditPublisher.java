package com.xiangyun.gateway;

import com.xiangyun.common.SecurityHeaders;
import com.xiangyun.common.TokenPayload;
import com.xiangyun.common.dto.AdminAuditRequest;
import com.xiangyun.common.security.InternalSignature;
import com.xiangyun.common.security.InternalSignatureSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class GatewayAuditPublisher {
    private static final Logger log = LoggerFactory.getLogger(GatewayAuditPublisher.class);
    private static final String AUDIT_PATH = "/api/internal/operation/audit-events";

    private final WebClient webClient;
    private final InternalSignatureSigner signer;

    public GatewayAuditPublisher(WebClient.Builder builder, InternalSignatureSigner signer) {
        this.webClient = builder.build();
        this.signer = signer;
    }

    public Mono<Void> accessDenied(ServerWebExchange exchange,
                                   TokenPayload payload,
                                   String traceId,
                                   String detail) {
        String requestPath = pathWithQuery(exchange);
        AdminAuditRequest event = new AdminAuditRequest(
                traceId,
                payload == null ? null : payload.userId(),
                payload == null ? "anonymous" : payload.username(),
                payload == null ? "ANONYMOUS" : payload.role(),
                payload == null ? null : payload.villageId(),
                "SECURITY",
                "ACCESS_DENIED",
                "ENDPOINT",
                exchange.getRequest().getURI().getPath(),
                exchange.getRequest().getMethod().name(),
                requestPath,
                clientIp(exchange),
                exchange.getRequest().getHeaders().getFirst("User-Agent"),
                "FAILURE",
                403,
                detail,
                null,
                null);
        InternalSignature signature = signer.sign("POST", AUDIT_PATH, traceId, "");
        return webClient.post()
                .uri("http://xiangyun-operation-service" + AUDIT_PATH)
                .header(SecurityHeaders.TRACE_ID, traceId)
                .header(SecurityHeaders.INTERNAL_SERVICE, signature.serviceName())
                .header(SecurityHeaders.INTERNAL_TIMESTAMP, signature.timestamp())
                .header(SecurityHeaders.INTERNAL_NONCE, signature.nonce())
                .header(SecurityHeaders.INTERNAL_SIGNATURE, signature.signature())
                .bodyValue(event)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(1))
                .then()
                .onErrorResume(ex -> {
                    log.warn("Failed to persist access-denied audit: traceId={}, path={}", traceId, requestPath);
                    return Mono.empty();
                });
    }

    private String pathWithQuery(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getRawPath();
        String query = exchange.getRequest().getURI().getRawQuery();
        return query == null || query.isBlank() ? path : path + "?" + query;
    }

    private String clientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() == null
                ? null
                : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }
}
