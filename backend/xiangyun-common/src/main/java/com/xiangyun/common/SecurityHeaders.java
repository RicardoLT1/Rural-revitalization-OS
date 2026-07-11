package com.xiangyun.common;

public final class SecurityHeaders {
    public static final String USER_ID = "X-User-Id";
    public static final String USERNAME = "X-Username";
    public static final String ROLE = "X-User-Role";
    public static final String VILLAGE_ID = "X-Village-Id";
    public static final String TRACE_ID = "X-Trace-Id";
    public static final String INTERNAL_SERVICE = "X-Internal-Service";
    public static final String INTERNAL_TIMESTAMP = "X-Internal-Timestamp";
    public static final String INTERNAL_NONCE = "X-Internal-Nonce";
    public static final String INTERNAL_SIGNATURE = "X-Internal-Signature";

    private SecurityHeaders() {
    }
}
