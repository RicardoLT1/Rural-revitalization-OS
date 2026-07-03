package com.xiangyun.auth;

import java.util.List;

public record DemoUser(String id, String username, String displayName, String role, String villageId, String passwordHash, boolean enabled, List<String> permissions) {
}
