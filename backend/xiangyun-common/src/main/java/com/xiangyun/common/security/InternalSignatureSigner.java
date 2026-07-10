package com.xiangyun.common.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

public class InternalSignatureSigner {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final InternalAuthProperties properties;
    private final Clock clock;

    public InternalSignatureSigner(InternalAuthProperties properties) {
        this(properties, Clock.systemUTC());
    }

    InternalSignatureSigner(InternalAuthProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public InternalSignature sign(String method, String pathWithQuery, String traceId, String bodyHash) {
        String timestamp = String.valueOf(Instant.now(clock).getEpochSecond());
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String serviceName = properties.getServiceName();
        String signature = hmac(canonical(method, pathWithQuery, timestamp, nonce, traceId, serviceName, bodyHash), properties.getSecret());
        return new InternalSignature(serviceName, timestamp, nonce, traceId, signature);
    }

    public String canonical(String method,
                            String pathWithQuery,
                            String timestamp,
                            String nonce,
                            String traceId,
                            String serviceName,
                            String bodyHash) {
        return String.join("\n",
                value(method).toUpperCase(),
                value(pathWithQuery),
                value(timestamp),
                value(nonce),
                value(traceId),
                value(serviceName),
                value(bodyHash));
    }

    public String hmac(String canonical, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(value(secret).getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new InternalAuthException("internal signature generation failed");
        }
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
