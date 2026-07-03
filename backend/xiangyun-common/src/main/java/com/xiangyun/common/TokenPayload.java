package com.xiangyun.common;

public record TokenPayload(
        String token,
        String jti,
        String userId,
        String username,
        String role,
        String villageId,
        long exp
) {
}
