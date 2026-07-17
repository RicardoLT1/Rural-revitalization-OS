package com.xiangyun.auth;

import com.xiangyun.common.BusinessException;
import com.xiangyun.common.dto.LoginResponse;
import com.xiangyun.common.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Set;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private SetOperations<String, String> setOperations;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        setOperations = mock(SetOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        authService = new AuthService(redisTemplate, "test-secret", 7200);
    }

    @Test
    void loginSuccessReturnsTokenAndRole() {
        LoginResponse response = authService.login("admin", "123456");
        assertThat(response.token()).isNotBlank();
        assertThat(response.user().role()).isEqualTo("ADMIN");
        verify(valueOperations, times(2)).set(anyString(), anyString(), any(Duration.class));
        verify(setOperations).add(anyString(), anyString());
    }

    @Test
    void userPageFiltersRoleAndReturnsMetadata() {
        PageResponse<Map<String, Object>> page = authService.userPage("demo", "STAFF", true, 1, 2);

        assertThat(page.page()).isEqualTo(1);
        assertThat(page.pageSize()).isEqualTo(2);
        assertThat(page.total()).isEqualTo(1);
        assertThat(page.items()).extracting(item -> item.get("role")).containsExactly("STAFF");
    }

    @Test
    void loginSupportsFinalDemoRoles() {
        assertThat(authService.login("user_demo", "123456").user().role()).isEqualTo("USER");
        assertThat(authService.login("staff_demo", "123456").user().role()).isEqualTo("STAFF");
        assertThat(authService.login("admin", "123456").user().role()).isEqualTo("ADMIN");
    }

    @Test
    void registerCreatesUserRoleAndToken() {
        LoginResponse response = authService.register("new_user", "123456", "新用户");

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().username()).isEqualTo("new_user");
        assertThat(response.user().role()).isEqualTo("USER");
        assertThat(authService.login("new_user", "123456").user().role()).isEqualTo("USER");
    }

    @Test
    void registerRejectsDuplicateUsername() {
        assertThatThrownBy(() -> authService.register("user_demo", "123456", "重复用户"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void loginRejectsWrongPassword() {
        assertThatThrownBy(() -> authService.login("admin", "bad"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void loginRejectsDisabledUser() {
        assertThatThrownBy(() -> authService.login("disabled", "123456"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void meReadsRedisSession() {
        LoginResponse response = authService.login("staff_demo", "123456");
        when(valueOperations.get(anyString())).thenReturn("2");
        LoginResponse.UserProfile profile = authService.me("Bearer " + response.token());
        assertThat(profile.role()).isEqualTo("STAFF");
    }

    @Test
    void logoutInvalidatesCurrentToken() {
        LoginResponse response = authService.login("staff_demo", "123456");

        authService.logout("Bearer " + response.token());

        verify(redisTemplate, times(2)).delete(anyString());
        verify(setOperations).remove(anyString(), anyString());
    }

    @Test
    void disableUserInvalidatesAllSessions() {
        when(setOperations.members("auth:user:sessions:2")).thenReturn(Set.of("jti-a", "jti-b"));

        authService.enableUser("2", false);

        verify(redisTemplate).delete("login:token:jti-a");
        verify(redisTemplate).delete("login:token:jti-b");
        verify(redisTemplate).delete("auth:user:sessions:2");
    }

    @Test
    void resetPasswordInvalidatesAllSessions() {
        when(setOperations.members("auth:user:sessions:2")).thenReturn(Set.of("jti-a"));

        authService.resetPassword("2", "123456");

        verify(redisTemplate).delete("login:token:jti-a");
        verify(redisTemplate).delete("auth:user:sessions:2");
    }

    @Test
    void assignRoleInvalidatesOldSessions() {
        when(setOperations.members("auth:user:sessions:2")).thenReturn(Set.of("jti-a"));

        authService.assignRole("2", "USER");

        verify(redisTemplate).delete("login:token:jti-a");
        verify(redisTemplate).delete("auth:user:sessions:2");
    }

    @Test
    void summaryReturnsUserInfo() {
        assertThat(authService.summary("1").role()).isEqualTo("USER");
        assertThat(authService.summary("2").role()).isEqualTo("STAFF");
        assertThat(authService.summary("3").role()).isEqualTo("ADMIN");
    }

    @Test
    void internalUserSearchHonorsVillageAndLimit() {
        var results = authService.searchUserSummaries("demo", "1", 2);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(user -> "1".equals(user.villageId()));
    }
}
