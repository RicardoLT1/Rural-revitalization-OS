package com.xiangyun.gateway;

import com.xiangyun.common.SecurityHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ResponseHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().remove(HttpHeaders.CONTENT_LENGTH);
            String traceId = exchange.getRequest().getHeaders().getFirst(SecurityHeaders.TRACE_ID);
            if (traceId != null && !traceId.isBlank()) {
                exchange.getResponse().getHeaders().set(SecurityHeaders.TRACE_ID, traceId);
            }
            return Mono.empty();
        });
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
