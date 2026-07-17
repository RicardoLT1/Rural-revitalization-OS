package com.xiangyun.auth;

import com.xiangyun.common.BusinessException;
import com.xiangyun.common.JwtUtils;
import com.xiangyun.common.TokenPayload;
import com.xiangyun.common.dto.LoginResponse;
import com.xiangyun.common.dto.PageResponse;
import com.xiangyun.common.dto.UserSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

@Service
public class AuthService {

    private static final List<String> USER_PERMISSIONS = List.of("resource:read", "workflow:start", "workflow:read");
    private static final List<String> STAFF_PERMISSIONS = List.of("resource:read", "workflow:read", "workflow:approve", "report:read");
    private static final List<String> ADMIN_PERMISSIONS = List.of("user:manage", "role:manage", "resource:write", "workflow:approve", "report:read", "system:read");

    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final String secret;
    private final long ttlSeconds;
    private final Map<String, DemoUser> users;

    public AuthService(StringRedisTemplate redisTemplate,
                       @Value("${xiangyun.jwt.secret}") String secret,
                       @Value("${xiangyun.jwt.ttl-seconds}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
        String password = encoder.encode("123456");
        this.users = createUsers(password);
    }

    public LoginResponse login(String username, String password) {
        DemoUser user = users.get(username);
        if (user == null || !encoder.matches(password, user.passwordHash())) {
            throw new BusinessException(40100, "用户名或密码错误");
        }
        if (!user.enabled()) {
            throw new BusinessException(40300, "用户已停用");
        }
        TokenPayload payload = JwtUtils.create(user.id(), user.username(), user.role(), user.villageId(), ttlSeconds, secret);
        registerSession(user.id(), payload.jti());
        return new LoginResponse(payload.token(), "Bearer", ttlSeconds, profile(user));
    }

    public synchronized LoginResponse register(String username, String password, String displayName) {
        if (!StringUtils.hasText(username) || username.length() < 3) {
            throw new BusinessException(40001, "用户名至少 3 个字符");
        }
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new BusinessException(40002, "密码至少 6 位");
        }
        if (users.containsKey(username)) {
            throw new BusinessException(40900, "用户名已存在");
        }
        String id = String.valueOf(users.size() + 1);
        String name = StringUtils.hasText(displayName) ? displayName : username;
        users.put(username, new DemoUser(id, username, name, "USER", "1", encoder.encode(password), true, USER_PERMISSIONS));
        return login(username, password);
    }

    public LoginResponse.UserProfile me(String authorization) {
        TokenPayload payload = parseBearer(authorization);
        assertSession(payload);
        DemoUser user = users.values().stream().filter(item -> item.id().equals(payload.userId())).findFirst()
                .orElseThrow(() -> new BusinessException(40100, "用户不存在"));
        return profile(user);
    }

    public void logout(String authorization) {
        TokenPayload payload = parseBearer(authorization);
        removeSession(payload.userId(), payload.jti());
    }

    public UserSummary summary(String id) {
        return users.values().stream()
                .filter(item -> item.id().equals(id))
                .map(item -> new UserSummary(item.id(), item.username(), item.displayName(), item.role(), item.villageId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(40400, "用户不存在"));
    }

    public List<UserSummary> listUsers() {
        return users.values().stream()
                .map(item -> new UserSummary(item.id(), item.username(), item.displayName(), item.role(), item.villageId()))
                .toList();
    }

    public List<Map<String, Object>> listUserRows() {
        return users.values().stream().map(this::userRow).toList();
    }

    public List<UserSummary> searchUserSummaries(String keyword, String villageId, Integer limit) {
        String term = keyword == null ? "" : keyword.trim().toLowerCase();
        int actualLimit = Math.max(1, Math.min(limit == null ? 5 : limit, 20));
        return users.values().stream()
                .filter(DemoUser::enabled)
                .filter(user -> !StringUtils.hasText(villageId) || villageId.equals(user.villageId()))
                .filter(user -> term.isEmpty()
                        || user.username().toLowerCase().contains(term)
                        || user.displayName().toLowerCase().contains(term))
                .sorted(Comparator.comparingInt(user -> Integer.parseInt(user.id())))
                .limit(actualLimit)
                .map(user -> new UserSummary(user.id(), user.username(), user.displayName(), user.role(), user.villageId()))
                .toList();
    }

    public PageResponse<Map<String, Object>> userPage(String keyword,
                                                      String role,
                                                      Boolean enabled,
                                                      Integer page,
                                                      Integer pageSize) {
        String term = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Map<String, Object>> filtered = users.values().stream()
                .filter(user -> term.isEmpty()
                        || user.username().toLowerCase().contains(term)
                        || user.displayName().toLowerCase().contains(term))
                .filter(user -> !StringUtils.hasText(role) || "ALL".equalsIgnoreCase(role) || user.role().equals(role))
                .filter(user -> enabled == null || user.enabled() == enabled)
                .sorted(Comparator.comparingInt(user -> Integer.parseInt(user.id())))
                .map(this::userRow)
                .toList();
        int actualPage = PageResponse.normalizePage(page);
        int actualPageSize = PageResponse.normalizePageSize(pageSize);
        int from = Math.min((actualPage - 1) * actualPageSize, filtered.size());
        int to = Math.min(from + actualPageSize, filtered.size());
        return PageResponse.of(filtered.subList(from, to), actualPage, actualPageSize, filtered.size());
    }

    public Map<String, Object> user(String id) {
        return userRow(findUser(id));
    }

    public synchronized Map<String, Object> createUser(Map<String, Object> body) {
        String username = bodyText(body, "username", "");
        if (!StringUtils.hasText(username) || username.length() < 3) {
            throw new BusinessException(40001, "用户名至少 3 个字符");
        }
        if (users.containsKey(username)) {
            throw new BusinessException(40900, "用户名已存在");
        }
        String password = bodyText(body, "password", "123456");
        if (password.length() < 6) {
            throw new BusinessException(40002, "密码至少 6 位");
        }
        String role = normalizeRole(bodyText(body, "role", "USER"));
        String displayName = bodyText(body, "displayName", username);
        String villageId = bodyText(body, "villageId", "1");
        DemoUser user = new DemoUser(nextUserId(), username, displayName, role, villageId, encoder.encode(password), true, permissionsForRole(role));
        users.put(username, user);
        return userRow(user);
    }

    public synchronized Map<String, Object> updateUser(String id, Map<String, Object> body) {
        DemoUser user = findUser(id);
        String role = body.containsKey("role") ? normalizeRole(bodyText(body, "role", user.role())) : user.role();
        boolean enabled = body.containsKey("enabled") ? Boolean.parseBoolean(String.valueOf(body.get("enabled"))) : user.enabled();
        DemoUser updated = new DemoUser(user.id(), user.username(), bodyText(body, "displayName", user.displayName()), role,
                bodyText(body, "villageId", user.villageId()), user.passwordHash(), enabled, enabled ? permissionsForRole(role) : List.of());
        users.put(updated.username(), updated);
        if (!enabled || !role.equals(user.role())) {
            invalidateUserSessions(id);
        }
        return userRow(updated);
    }

    public synchronized Map<String, Object> enableUser(String id, boolean enabled) {
        DemoUser user = findUser(id);
        DemoUser updated = new DemoUser(user.id(), user.username(), user.displayName(), user.role(), user.villageId(),
                user.passwordHash(), enabled, enabled ? permissionsForRole(user.role()) : List.of());
        users.put(updated.username(), updated);
        if (!enabled) {
            invalidateUserSessions(id);
        }
        return userRow(updated);
    }

    public synchronized Map<String, Object> resetPassword(String id, String password) {
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new BusinessException(40002, "密码至少 6 位");
        }
        DemoUser user = findUser(id);
        DemoUser updated = new DemoUser(user.id(), user.username(), user.displayName(), user.role(), user.villageId(),
                encoder.encode(password), user.enabled(), user.permissions());
        users.put(updated.username(), updated);
        invalidateUserSessions(id);
        return Map.of("id", id, "passwordChanged", true, "sessionsInvalidated", true);
    }

    public synchronized Map<String, Object> assignRole(String id, String role) {
        DemoUser user = findUser(id);
        String actualRole = normalizeRole(role);
        DemoUser updated = new DemoUser(user.id(), user.username(), user.displayName(), actualRole, user.villageId(),
                user.passwordHash(), user.enabled(), user.enabled() ? permissionsForRole(actualRole) : List.of());
        users.put(updated.username(), updated);
        if (!actualRole.equals(user.role())) {
            invalidateUserSessions(id);
        }
        return userRow(updated);
    }

    public List<Map<String, Object>> roles() {
        return List.of(
                Map.of("code", "USER", "name", "普通用户"),
                Map.of("code", "STAFF", "name", "工作人员"),
                Map.of("code", "ADMIN", "name", "系统管理员")
        );
    }

    private void registerSession(String userId, String jti) {
        redisTemplate.opsForValue().set(sessionKey(jti), userId, Duration.ofSeconds(ttlSeconds));
        redisTemplate.opsForValue().set(authSessionKey(jti), userId, Duration.ofSeconds(ttlSeconds));
        redisTemplate.opsForSet().add(userSessionsKey(userId), jti);
        redisTemplate.expire(userSessionsKey(userId), Duration.ofSeconds(ttlSeconds));
    }

    private void removeSession(String userId, String jti) {
        redisTemplate.delete(sessionKey(jti));
        redisTemplate.delete(authSessionKey(jti));
        redisTemplate.opsForSet().remove(userSessionsKey(userId), jti);
    }

    private void invalidateUserSessions(String userId) {
        String key = userSessionsKey(userId);
        Set<String> sessions = redisTemplate.opsForSet().members(key);
        if (sessions != null) {
            sessions.forEach(jti -> {
                redisTemplate.delete(sessionKey(jti));
                redisTemplate.delete(authSessionKey(jti));
            });
        }
        redisTemplate.delete(key);
    }

    private Map<String, Object> userRow(DemoUser user) {
        return Map.of("id", user.id(), "username", user.username(), "displayName", user.displayName(),
                "role", user.role(), "villageId", user.villageId(), "enabled", user.enabled());
    }

    private DemoUser findUser(String id) {
        return users.values().stream().filter(item -> item.id().equals(id)).findFirst()
                .orElseThrow(() -> new BusinessException(40400, "用户不存在"));
    }

    private String nextUserId() {
        int max = users.values().stream().map(DemoUser::id).mapToInt(value -> {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }).max().orElse(0);
        return String.valueOf(max + 1);
    }

    private String normalizeRole(String role) {
        String actualRole = String.valueOf(role == null ? "" : role).trim().toUpperCase();
        if (!List.of("USER", "STAFF", "ADMIN").contains(actualRole)) {
            throw new BusinessException(40003, "角色不合法");
        }
        return actualRole;
    }

    private List<String> permissionsForRole(String role) {
        return switch (normalizeRole(role)) {
            case "ADMIN" -> ADMIN_PERMISSIONS;
            case "STAFF" -> STAFF_PERMISSIONS;
            default -> USER_PERMISSIONS;
        };
    }

    private String bodyText(Map<String, Object> body, String key, String fallback) {
        Object value = body.get(key);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return fallback;
        }
        return String.valueOf(value).trim();
    }

    private Map<String, DemoUser> createUsers(String password) {
        Map<String, DemoUser> demoUsers = new LinkedHashMap<>();
        demoUsers.put("admin", new DemoUser("3", "admin", "系统管理员", "ADMIN", "1", password, true, ADMIN_PERMISSIONS));
        demoUsers.put("user_demo", new DemoUser("1", "user_demo", "小程序用户", "USER", "1", password, true, USER_PERMISSIONS));
        demoUsers.put("staff_demo", new DemoUser("2", "staff_demo", "业务工作人员", "STAFF", "1", password, true, STAFF_PERMISSIONS));
        demoUsers.put("operator", new DemoUser("4", "operator", "兼容运营账号", "STAFF", "1", password, true, STAFF_PERMISSIONS));
        demoUsers.put("approver", new DemoUser("5", "approver", "兼容审批账号", "STAFF", "1", password, true, STAFF_PERMISSIONS));
        demoUsers.put("viewer", new DemoUser("6", "viewer", "兼容查看账号", "STAFF", "1", password, true, STAFF_PERMISSIONS));
        demoUsers.put("disabled", new DemoUser("7", "disabled", "停用账号", "USER", "1", password, false, List.of()));
        return demoUsers;
    }

    private LoginResponse.UserProfile profile(DemoUser user) {
        return new LoginResponse.UserProfile(user.id(), user.username(), user.displayName(), user.role(), user.villageId(), user.permissions());
    }

    private void assertSession(TokenPayload payload) {
        if (redisTemplate.opsForValue().get(sessionKey(payload.jti())) == null) {
            throw new BusinessException(40100, "登录会话已失效");
        }
    }

    private TokenPayload parseBearer(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(40100, "未登录");
        }
        return JwtUtils.parse(authorization.substring(7), secret);
    }

    private String sessionKey(String jti) {
        return "login:token:" + jti;
    }

    private String authSessionKey(String jti) {
        return "auth:session:" + jti;
    }

    private String userSessionsKey(String userId) {
        return "auth:user:sessions:" + userId;
    }
}
