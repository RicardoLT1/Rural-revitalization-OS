package com.xiangyun.gateway;

import com.xiangyun.common.SecurityHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseHeaderFilterTest {

    private final ResponseHeaderFilter filter = new ResponseHeaderFilter();

    @Test
    void writesTraceIdToResponseHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/dashboard")
                .header(SecurityHeaders.TRACE_ID, "trace-001"));
        GatewayFilterChain chain = next -> {
            next.getResponse().setStatusCode(HttpStatus.OK);
            return next.getResponse().setComplete();
        };

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getHeaders().getFirst(SecurityHeaders.TRACE_ID)).isEqualTo("trace-001");
    }
}
