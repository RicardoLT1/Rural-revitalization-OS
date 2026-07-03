package com.xiangyun.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class JwtUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private JwtUtils() {
    }

    public static TokenPayload create(String userId, String username, String role, String villageId, long ttlSeconds, String secret) {
        String jti = UUID.randomUUID().toString().replace("-", "");
        long exp = Instant.now().getEpochSecond() + ttlSeconds;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("jti", jti);
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("role", role);
        payload.put("villageId", villageId);
        payload.put("exp", exp);
        String token = encodeJson(Map.of("alg", "HS256", "typ", "JWT")) + "." + encodeJson(payload);
        token = token + "." + sign(token, secret);
        return new TokenPayload(token, jti, userId, username, role, villageId, exp);
    }

    public static TokenPayload parse(String token, String secret) {
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if (parts.length != 3) {
            throw new BusinessException(40100, "无效Token");
        }
        String signingInput = parts[0] + "." + parts[1];
        if (!sign(signingInput, secret).equals(parts[2])) {
            throw new BusinessException(40100, "Token签名无效");
        }
        try {
            Map<String, Object> payload = MAPPER.readValue(DECODER.decode(parts[1]), new TypeReference<>() {
            });
            long exp = Long.parseLong(String.valueOf(payload.get("exp")));
            if (exp < Instant.now().getEpochSecond()) {
                throw new BusinessException(40100, "Token已过期");
            }
            return new TokenPayload(token,
                    String.valueOf(payload.get("jti")),
                    String.valueOf(payload.get("userId")),
                    String.valueOf(payload.get("username")),
                    String.valueOf(payload.get("role")),
                    String.valueOf(payload.get("villageId")),
                    exp);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(40100, "Token解析失败");
        }
    }

    private static String encodeJson(Object value) {
        try {
            return ENCODER.encodeToString(MAPPER.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("JWT编码失败", ex);
        }
    }

    private static String sign(String input, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return ENCODER.encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("JWT签名失败", ex);
        }
    }
}
