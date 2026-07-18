package com.xiangyun.auth;

import com.xiangyun.common.BusinessException;
import com.xiangyun.common.dto.LoginResponse;
import com.xiangyun.common.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    private AuthUserRepository repository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        setOperations = mock(SetOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:auth-" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "sa", "");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                create table auth_user(
                  id bigint auto_increment primary key,
                  username varchar(64) not null unique,
                  display_name varchar(64) not null,
                  role_code varchar(32) not null,
                  village_id varchar(64) not null,
                  password_hash varchar(100) not null,
                  enabled tinyint not null default 1,
                  created_at timestamp not null default current_timestamp,
                  updated_at timestamp not null default current_timestamp
                )
                """);
        repository = new AuthUserRepository(jdbcTemplate);
        seed(repository, "user_demo", "小程序用户", "USER", true);
        seed(repository, "staff_demo", "业务工作人员", "STAFF", true);
        seed(repository, "admin", "系统管理员", "ADMIN", true);
        seed(repository, "operator", "兼容运营账号", "STAFF", true);
        seed(repository, "approver", "兼容审批账号", "STAFF", true);
        seed(repository, "viewer", "兼容查看账号", "STAFF", true);
        seed(repository, "disabled", "停用账号", "USER", false);
        authService = new AuthService(repository, redisTemplate, "test-secret", 7200);
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
    void registerCreatesPersistedUserWithDatabaseGeneratedId() {
        LoginResponse response = authService.register("new_user", "123456", "新用户");

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().id()).isEqualTo("8");
        assertThat(response.user().role()).isEqualTo("USER");
        assertThat(new AuthService(repository, redisTemplate, "test-secret", 7200)
                .login("new_user", "123456").user().username()).isEqualTo("new_user");
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
    void disableUserInvalidatesAllSessionsAndPersistsStatus() {
        when(setOperations.members("auth:user:sessions:2")).thenReturn(Set.of("jti-a", "jti-b"));

        authService.enableUser("2", false);

        verify(redisTemplate).delete("login:token:jti-a");
        verify(redisTemplate).delete("login:token:jti-b");
        verify(redisTemplate).delete("auth:user:sessions:2");
        AuthService restarted = new AuthService(repository, redisTemplate, "test-secret", 7200);
        assertThat(restarted.user("2")).containsEntry("enabled", false);
    }

    @Test
    void resetPasswordInvalidatesAllSessions() {
        when(setOperations.members("auth:user:sessions:2")).thenReturn(Set.of("jti-a"));

        authService.resetPassword("2", "123456");

        verify(redisTemplate).delete("login:token:jti-a");
        verify(redisTemplate).delete("auth:user:sessions:2");
    }

    @Test
    void assignRoleInvalidatesOldSessionsAndPersistsRole() {
        when(setOperations.members("auth:user:sessions:2")).thenReturn(Set.of("jti-a"));

        authService.assignRole("2", "USER");

        verify(redisTemplate).delete("login:token:jti-a");
        verify(redisTemplate).delete("auth:user:sessions:2");
        AuthService restarted = new AuthService(repository, redisTemplate, "test-secret", 7200);
        assertThat(restarted.summary("2").role()).isEqualTo("USER");
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

    @Test
    void currentUserCanUpdateDisplayNameAndChangeSurvivesServiceRebuild() {
        LoginResponse response = authService.login("staff_demo", "123456");
        when(valueOperations.get(anyString())).thenReturn("2");

        var profile = authService.updateOwnProfile(
                "Bearer " + response.token(), Map.of("displayName", "村域运营专员"));

        assertThat(profile.displayName()).isEqualTo("村域运营专员");
        AuthService restarted = new AuthService(repository, redisTemplate, "test-secret", 7200);
        assertThat(restarted.summary("2").displayName()).isEqualTo("村域运营专员");
    }

    @Test
    void changingOwnPasswordPersistsAndInvalidatesSessions() {
        LoginResponse response = authService.login("staff_demo", "123456");
        when(valueOperations.get(anyString())).thenReturn("2");
        when(setOperations.members("auth:user:sessions:2")).thenReturn(Set.of("jti-a"));

        Map<String, Object> result = authService.changeOwnPassword(
                "Bearer " + response.token(), "123456", "NewPass2026");

        assertThat(result).containsEntry("passwordChanged", true).containsEntry("sessionsInvalidated", true);
        verify(redisTemplate).delete("auth:user:sessions:2");
        AuthService restarted = new AuthService(repository, redisTemplate, "test-secret", 7200);
        assertThatThrownBy(() -> restarted.login("staff_demo", "123456"))
                .isInstanceOf(BusinessException.class);
        assertThat(restarted.login("staff_demo", "NewPass2026").user().id()).isEqualTo("2");
    }

    @Test
    void changingOwnPasswordRejectsWrongCurrentPassword() {
        LoginResponse response = authService.login("staff_demo", "123456");
        when(valueOperations.get(anyString())).thenReturn("2");

        assertThatThrownBy(() -> authService.changeOwnPassword(
                "Bearer " + response.token(), "wrong", "NewPass2026"))
                .isInstanceOf(BusinessException.class);
    }

    private void seed(AuthUserRepository target,
                      String username,
                      String displayName,
                      String role,
                      boolean enabled) {
        target.create(username, displayName, role, "1",
                new BCryptPasswordEncoder().encode("123456"), enabled);
    }
}
