package com.xiangyun.common.security;

import com.xiangyun.common.SecurityHeaders;
import feign.RequestTemplate;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InternalSignatureTest {
    private InternalAuthProperties properties;
    private InternalSignatureSigner signer;
    private InternalSignatureVerifier verifier;

    @BeforeEach
    void setUp() {
        properties = new InternalAuthProperties();
        properties.setServiceName("xiangyun-gateway");
        properties.setSecret("internal-secret");
        properties.setAllowedServices(List.of("xiangyun-gateway"));
        signer = new InternalSignatureSigner(properties);
        verifier = new InternalSignatureVerifier(properties, signer);
    }

    @Test
    void verifiesRequestWithQueryString() {
        InternalSignature signature = signer.sign("GET", "/api/resources?id=1", "trace-001", "");

        verifier.verify("GET", "/api/resources?id=1", signature.timestamp(), signature.nonce(),
                signature.traceId(), signature.serviceName(), "", signature.signature(), new InMemoryInternalNonceStore());
    }

    @Test
    void rejectsTamperedQueryString() {
        InternalSignature signature = signer.sign("GET", "/api/resources?id=1", "trace-001", "");

        assertThatThrownBy(() -> verifier.verify("GET", "/api/resources?id=2", signature.timestamp(), signature.nonce(),
                signature.traceId(), signature.serviceName(), "", signature.signature(), new InMemoryInternalNonceStore()))
                .isInstanceOf(InternalAuthException.class)
                .hasMessageContaining("invalid internal signature");
    }

    @Test
    void rejectsReplayedNonce() {
        InternalSignature signature = signer.sign("GET", "/api/resources?id=1", "trace-001", "");
        InMemoryInternalNonceStore nonceStore = new InMemoryInternalNonceStore();
        verifier.verify("GET", "/api/resources?id=1", signature.timestamp(), signature.nonce(),
                signature.traceId(), signature.serviceName(), "", signature.signature(), nonceStore);

        assertThatThrownBy(() -> verifier.verify("GET", "/api/resources?id=1", signature.timestamp(), signature.nonce(),
                signature.traceId(), signature.serviceName(), "", signature.signature(), nonceStore))
                .isInstanceOf(InternalAuthException.class)
                .hasMessageContaining("replayed internal nonce");
    }

    @Test
    void feignInterceptorSignsInternalRequest() {
        RequestTemplate template = new RequestTemplate();
        template.method("GET");
        template.uri("/api/internal/operation/stats?villageId=1");

        new InternalFeignRequestInterceptor(signer).apply(template);

        assertThat(template.headers().get(SecurityHeaders.INTERNAL_SERVICE)).contains("xiangyun-gateway");
        assertThat(template.headers().get(SecurityHeaders.INTERNAL_SIGNATURE)).isNotEmpty();
        assertThat(template.headers().get(SecurityHeaders.TRACE_ID)).isNotEmpty();
    }

    @Test
    void filterSkipsExcludedHealthPath() throws ServletException, IOException {
        properties.setExcludePaths(List.of("/actuator/health"));
        InternalAuthFilter filter = new InternalAuthFilter(properties, verifier, new InMemoryInternalNonceStore(), new com.fasterxml.jackson.databind.ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void filterRejectsMissingSignature() throws ServletException, IOException {
        InternalAuthFilter filter = new InternalAuthFilter(properties, verifier, new InMemoryInternalNonceStore(), new com.fasterxml.jackson.databind.ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/resources");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(403);
    }
}
