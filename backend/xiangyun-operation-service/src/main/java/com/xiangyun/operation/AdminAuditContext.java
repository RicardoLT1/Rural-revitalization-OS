package com.xiangyun.operation;

import jakarta.servlet.http.HttpServletRequest;

final class AdminAuditContext {
    static final String BEFORE_DATA = AdminAuditContext.class.getName() + ".beforeData";
    static final String AFTER_DATA = AdminAuditContext.class.getName() + ".afterData";
    static final String TARGET_ID = AdminAuditContext.class.getName() + ".targetId";

    private AdminAuditContext() {
    }

    static void before(HttpServletRequest request, Object value) {
        request.setAttribute(BEFORE_DATA, value);
    }

    static void after(HttpServletRequest request, Object value) {
        request.setAttribute(AFTER_DATA, value);
    }

    static void targetId(HttpServletRequest request, Object value) {
        if (value != null) {
            request.setAttribute(TARGET_ID, String.valueOf(value));
        }
    }
}
