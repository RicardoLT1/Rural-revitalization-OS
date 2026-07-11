package com.xiangyun.common.security;

import com.xiangyun.common.SecurityHeaders;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.Collection;
import java.util.UUID;

public class InternalFeignRequestInterceptor implements RequestInterceptor {
    private final InternalSignatureSigner signer;

    public InternalFeignRequestInterceptor(InternalSignatureSigner signer) {
        this.signer = signer;
    }

    @Override
    public void apply(RequestTemplate template) {
        String traceId = first(template.headers().get(SecurityHeaders.TRACE_ID));
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        InternalSignature signature = signer.sign(template.method(), template.url(), traceId, "");
        template.header(SecurityHeaders.INTERNAL_SERVICE);
        template.header(SecurityHeaders.INTERNAL_TIMESTAMP);
        template.header(SecurityHeaders.INTERNAL_NONCE);
        template.header(SecurityHeaders.INTERNAL_SIGNATURE);
        template.header(SecurityHeaders.TRACE_ID);
        template.header(SecurityHeaders.INTERNAL_SERVICE, signature.serviceName());
        template.header(SecurityHeaders.INTERNAL_TIMESTAMP, signature.timestamp());
        template.header(SecurityHeaders.INTERNAL_NONCE, signature.nonce());
        template.header(SecurityHeaders.INTERNAL_SIGNATURE, signature.signature());
        template.header(SecurityHeaders.TRACE_ID, traceId);
    }

    private String first(Collection<String> values) {
        return values == null || values.isEmpty() ? null : values.iterator().next();
    }
}
