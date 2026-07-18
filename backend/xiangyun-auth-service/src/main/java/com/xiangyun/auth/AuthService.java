package com.xiangyun.auth;

import com.xiangyun.common.BusinessException;
import com.xiangyun.common.JwtUtils;
import com.xiangyun.common.TokenPayload;
import com.xiangyun.common.dto.LoginResponse;
import com.xiangyun.common.dto.PageResponse;
import com.xiangyun.common.dto.UserSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    private static final List<String> USER_PERMISSIONS = List.of("resource:read", "workflow:start", "workflow:read");
    private static final List<String> STAFF_PERMISSIONS = List.of("resource:read", "workflow:read", "workflow:approve", "report:read");
    private static final List<String> ADMIN_PERMISSIONS = List.of("user:manage", "role:manage", "resource:write", "workflow:approve", "report:read", "system:read");

    private final AuthUserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final String secret;
    private final long ttlSeconds;

    public AuthService(AuthUserRepository userRepository,
                       StringRedisTemplate redisTemplate,
                       @Value("${xiangyun.jwt.secret}") String secret,
                       @Value("${xiangyun.jwt.ttl-seconds}") long ttlSeconds) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    public LoginResponse login(String username, String password) {
        AuthUserAccount user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !StringUtils.hasText(password) || !encoder.matches(password, user.passwordHash())) {
            throw new BusinessException(40100, "用户名或密码错误");
        }
        if (!user.enabled()) {
            throw new BusinessException(40300, "用户已停用");
        }
        TokenPayload payload = JwtUtils.create(user.id(), user.username(), user.role(), user.villageId(), ttlSeconds, secret);
        registerSession(user.id(), payload.jti());
        return new LoginResponse(payload.token(), "Bearer", ttlSeconds, profile(user));
    }

    public LoginResponse register(String username, String password, String displayName) {
        String actualUsername = username == null ? "" : username.trim();
        if (actualUsername.length() < 3 || actualUsername.length() > 64) {
            throw new BusinessException(40001, "用户名长度应在 3 到 64 个字符之间");
        }
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new BusinessException(40002, "密码至少 6 位");
        }
        if (userRepository.existsByUsername(actualUsername)) {
            throw new BusinessException(40900, "用户名已存在");
        }
        String name = StringUtils.hasText(displayName) ? displayName.trim() : actualUsername;
        requireLength(name, 1, 64, 40004, "姓名");
        createAccount(actualUsername, name, "USER", "1", password, true);
        return login(actualUsername, password);
    }

    public LoginResponse.UserProfile me(String authorization) {
        return profile(authenticatedUser(authorization));
    }

    public LoginResponse.UserProfile updateOwnProfile(String authorization, Map<String, Object> body) {
        AuthUserAccount user = authenticatedUser(authorization);
        String displayName = bodyText(body, "displayName", user.displayName());
        if (displayName.length() < 2 || displayName.length() > 32) {
            throw new BusinessException(40004, "姓名长度应在 2 到 32 个字符之间");
        }
        userRepository.updateDisplayName(user.id(), displayName);
        return profile(findUser(user.id()));
    }

    public Map<String, Object> changeOwnPassword(String authorization,
                                                  String currentPassword,
                                                  String newPassword) {
        AuthUserAccount user = authenticatedUser(authorization);
        if (!StringUtils.hasText(currentPassword) || !encoder.matches(currentPassword, user.passwordHash())) {
            throw new BusinessException(40005, "当前密码不正确");
        }
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 8) {
            throw new BusinessException(40006, "新密码至少 8 位");
        }
        if (encoder.matches(newPassword, user.passwordHash())) {
            throw new BusinessException(40901, "新密码不能与当前密码相同");
        }
        userRepository.updatePasswordHash(user.id(), encoder.encode(newPassword));
        invalidateUserSessions(user.id());
        return Map.of("id", user.id(), "passwordChanged", true, "sessionsInvalidated", true);
    }

    public void logout(String authorization) {
        TokenPayload payload = parseBearer(authorization);
        removeSession(payload.userId(), payload.jti());
    }

    public UserSummary summary(String id) {
        AuthUserAccount user = findUser(id);
        return new UserSummary(user.id(), user.username(), user.displayName(), user.role(), user.villageId());
    }

    public List<UserSummary> listUsers() {
        return userRepository.findAll().stream()
                .map(item -> new UserSummary(item.id(), item.username(), item.displayName(), item.role(), item.villageId()))
                .toList();
    }

    public List<Map<String, Object>> listUserRows() {
        return userRepository.findAll().stream().map(this::userRow).toList();
    }

    public List<UserSummary> searchUserSummaries(String keyword, String villageId, Integer limit) {
        String term = keyword == null ? "" : keyword.trim().toLowerCase();
        int actualLimit = Math.max(1, Math.min(limit == null ? 5 : limit, 20));
        return userRepository.findAll().stream()
                .filter(AuthUserAccount::enabled)
                .filter(user -> !StringUtils.hasText(villageId) || villageId.equals(user.villageId()))
                .filter(user -> term.isEmpty()
                        || user.username().toLowerCase().contains(term)
                        || user.displayName().toLowerCase().contains(term))
                .sorted(Comparator.comparingLong(this::numericId))
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
        List<Map<String, Object>> filtered = userRepository.findAll().stream()
                .filter(user -> term.isEmpty()
                        || user.username().toLowerCase().contains(term)
                        || user.displayName().toLowerCase().contains(term))
                .filter(user -> !StringUtils.hasText(role) || "ALL".equalsIgnoreCase(role) || user.role().equals(role))
                .filter(user -> enabled == null || user.enabled() == enabled)
                .sorted(Comparator.comparingLong(this::numericId))
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

    public Map<String, Object> createUser(Map<String, Object> body) {
        String username = bodyText(body, "username", "");
        if (username.length() < 3 || username.length() > 64) {
            throw new BusinessException(40001, "用户名长度应在 3 到 64 个字符之间");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(40900, "用户名已存在");
        }
        String password = bodyText(body, "password", "123456");
        if (password.length() < 6) {
            throw new BusinessException(40002, "密码至少 6 位");
        }
        String role = normalizeRole(bodyText(body, "role", "USER"));
        String displayName = bodyText(body, "displayName", username);
        String villageId = bodyText(body, "villageId", "1");
        requireLength(displayName, 1, 64, 40004, "姓名");
        requireLength(villageId, 1, 64, 40010, "村域编号");
        return userRow(createAccount(username, displayName, role, villageId, password, true));
    }

    public Map<String, Object> updateUser(String id, Map<String, Object> body) {
        AuthUserAccount user = findUser(id);
        String role = body.containsKey("role") ? normalizeRole(bodyText(body, "role", user.role())) : user.role();
        boolean enabled = body.containsKey("enabled") ? Boolean.parseBoolean(String.valueOf(body.get("enabled"))) : user.enabled();
        String displayName = bodyText(body, "displayName", user.displayName());
        String villageId = bodyText(body, "villageId", user.villageId());
        requireLength(displayName, 1, 64, 40004, "姓名");
        requireLength(villageId, 1, 64, 40010, "村域编号");
        userRepository.updateAccount(id,
                displayName,
                role,
                villageId,
                enabled);
        if (!enabled || !role.equals(user.role())) {
            invalidateUserSessions(id);
        }
        return userRow(findUser(id));
    }

    public Map<String, Object> enableUser(String id, boolean enabled) {
        findUser(id);
        userRepository.updateEnabled(id, enabled);
        if (!enabled) {
            invalidateUserSessions(id);
        }
        return userRow(findUser(id));
    }

    public Map<String, Object> resetPassword(String id, String password) {
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new BusinessException(40002, "密码至少 6 位");
        }
        findUser(id);
        userRepository.updatePasswordHash(id, encoder.encode(password));
        invalidateUserSessions(id);
        return Map.of("id", id, "passwordChanged", true, "sessionsInvalidated", true);
    }

    public Map<String, Object> assignRole(String id, String role) {
        AuthUserAccount user = findUser(id);
        String actualRole = normalizeRole(role);
        userRepository.updateRole(id, actualRole);
        if (!actualRole.equals(user.role())) {
            invalidateUserSessions(id);
        }
        return userRow(findUser(id));
    }

    public List<Map<String, Object>> roles() {
        return List.of(
                Map.of("code", "USER", "name", "普通用户"),
                Map.of("code", "STAFF", "name", "工作人员"),
                Map.of("code", "ADMIN", "name", "系统管理员")
        );
    }

    private AuthUserAccount createAccount(String username,
                                          String displayName,
                                          String role,
                                          String villageId,
                                          String rawPassword,
                                          boolean enabled) {
        try {
            return userRepository.create(username, displayName, role, villageId, encoder.encode(rawPassword), enabled);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(40900, "用户名已存在");
        } catch (DataIntegrityViolationException ex) {
            if (userRepository.existsByUsername(username)) {
                throw new BusinessException(40900, "用户名已存在");
            }
            throw ex;
        }
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

    private Map<String, Object> userRow(AuthUserAccount user) {
        return Map.of("id", user.id(), "username", user.username(), "displayName", user.displayName(),
                "role", user.role(), "villageId", user.villageId(), "enabled", user.enabled());
    }

    private AuthUserAccount findUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "用户不存在"));
    }

    private long numericId(AuthUserAccount user) {
        try {
            return Long.parseLong(user.id());
        } catch (NumberFormatException ex) {
            return Long.MAX_VALUE;
        }
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

    private void requireLength(String value, int min, int max, int code, String label) {
        if (value == null || value.length() < min || value.length() > max) {
            throw new BusinessException(code, label + "长度应在 " + min + " 到 " + max + " 个字符之间");
        }
    }

    private LoginResponse.UserProfile profile(AuthUserAccount user) {
        return new LoginResponse.UserProfile(user.id(), user.username(), user.displayName(), user.role(),
                user.villageId(), user.enabled() ? permissionsForRole(user.role()) : List.of());
    }

    private void assertSession(TokenPayload payload) {
        if (redisTemplate.opsForValue().get(sessionKey(payload.jti())) == null) {
            throw new BusinessException(40100, "登录会话已失效");
        }
    }

    private AuthUserAccount authenticatedUser(String authorization) {
        TokenPayload payload = parseBearer(authorization);
        assertSession(payload);
        return userRepository.findById(payload.userId())
                .orElseThrow(() -> new BusinessException(40100, "用户不存在"));
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
