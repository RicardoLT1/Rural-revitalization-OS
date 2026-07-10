package com.xiangyun.common.security;

import java.time.Clock;
import java.time.Instant;

public class InternalSignatureVerifier {
    private final InternalAuthProperties properties;
    private final InternalSignatureSigner signer;
    private final Clock clock;

    public InternalSignatureVerifier(InternalAuthProperties properties, InternalSignatureSigner signer) {
        this(properties, signer, Clock.systemUTC());
    }

    InternalSignatureVerifier(InternalAuthProperties properties, InternalSignatureSigner signer, Clock clock) {
        this.properties = properties;
        this.signer = signer;
        this.clock = clock;
    }

    public void verify(String method,
                       String pathWithQuery,
                       String timestamp,
                       String nonce,
                       String traceId,
                       String serviceName,
                       String bodyHash,
                       String signature,
                       InternalNonceStore nonceStore) {
        require(timestamp, "missing internal timestamp");
        require(nonce, "missing internal nonce");
        require(traceId, "missing trace id");
        require(serviceName, "missing internal service");
        require(signature, "missing internal signature");
        if (!properties.getAllowedServices().isEmpty() && !properties.getAllowedServices().contains(serviceName)) {
            throw new InternalAuthException("internal service is not allowed");
        }
        long issuedAt = parseTimestamp(timestamp);
        long now = Instant.now(clock).getEpochSecond();
        if (Math.abs(now - issuedAt) > properties.getTtlSeconds() + properties.getClockSkewSeconds()) {
            throw new InternalAuthException("internal signature expired");
        }
        String expected = signer.hmac(
                signer.canonical(method, pathWithQuery, timestamp, nonce, traceId, serviceName, bodyHash),
                properties.getSecret());
        if (!constantTimeEquals(expected, signature)) {
            throw new InternalAuthException("invalid internal signature");
        }
        if (nonceStore != null && !nonceStore.markIfNew(serviceName, nonce, properties.nonceTtlSeconds())) {
            throw new InternalAuthException("replayed internal nonce");
        }
    }

    private long parseTimestamp(String timestamp) {
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException ex) {
            throw new InternalAuthException("invalid internal timestamp");
        }
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InternalAuthException(message);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null || left.length() != right.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length(); i++) {
            result |= left.charAt(i) ^ right.charAt(i);
        }
        return result == 0;
    }
}
