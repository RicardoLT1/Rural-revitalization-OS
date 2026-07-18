package com.xiangyun.auth;

public record AuthUserAccount(
        String id,
        String username,
        String displayName,
        String role,
        String villageId,
        String passwordHash,
        boolean enabled
) {
}
