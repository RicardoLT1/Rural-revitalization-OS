package com.xiangyun.common.security;

public record InternalSignature(
        String serviceName,
        String timestamp,
        String nonce,
        String traceId,
        String signature
) {
}
