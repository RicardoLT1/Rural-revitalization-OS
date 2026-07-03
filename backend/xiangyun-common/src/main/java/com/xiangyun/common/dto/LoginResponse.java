package com.xiangyun.common.dto;

import java.util.List;

public record LoginResponse(String token, String tokenType, long expiresIn, UserProfile user) {
    public record UserProfile(String id, String username, String displayName, String role, String villageId, List<String> permissions) {
    }
}
