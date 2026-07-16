package com.xiangyun.auth;

import com.xiangyun.common.BusinessException;
import com.xiangyun.common.dto.LoginRequest;
import com.xiangyun.common.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerAuditTest {

    @Test
    void recordsSuccessfulLogin() {
        AuthService authService = mock(AuthService.class);
        AuthAuditPublisher auditPublisher = mock(AuthAuditPublisher.class);
        AuthController controller = new AuthController(authService, auditPublisher);
        LoginResponse response = new LoginResponse("token", "Bearer", 7200,
                new LoginResponse.UserProfile("3", "admin", "系统管理员", "ADMIN", "1", List.of("user:manage")));
        when(authService.login("admin", "123456")).thenReturn(response);

        var result = controller.login(new LoginRequest("admin", "123456"),
                new MockHttpServletRequest("POST", "/api/auth/login"));

        assertThat(result.data()).isEqualTo(response);
        verify(auditPublisher).recordSecurity(any(), eq("LOGIN_SUCCESS"), eq("3"), eq("admin"),
                eq("ADMIN"), eq("1"), eq("3"), eq("SUCCESS"), eq(200), eq("登录成功"));
    }

    @Test
    void recordsFailedLogin() {
        AuthService authService = mock(AuthService.class);
        AuthAuditPublisher auditPublisher = mock(AuthAuditPublisher.class);
        AuthController controller = new AuthController(authService, auditPublisher);
        when(authService.login("unknown", "bad-password"))
                .thenThrow(new BusinessException(40100, "账号或密码错误"));

        assertThatThrownBy(() -> controller.login(new LoginRequest("unknown", "bad-password"),
                new MockHttpServletRequest("POST", "/api/auth/login")))
                .isInstanceOf(BusinessException.class);

        verify(auditPublisher).recordSecurity(any(), eq("LOGIN_FAILURE"), isNull(), eq("unknown"),
                eq("ANONYMOUS"), isNull(), eq("unknown"), eq("FAILURE"), eq(401), eq("账号或密码错误"));
    }
}
