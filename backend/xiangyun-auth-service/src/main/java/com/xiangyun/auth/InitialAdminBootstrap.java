package com.xiangyun.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(name = "xiangyun.auth.bootstrap.enabled", havingValue = "true")
public class InitialAdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminBootstrap.class);

    private final AuthUserRepository repository;
    private final String username;
    private final String password;
    private final String displayName;
    private final String villageId;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public InitialAdminBootstrap(AuthUserRepository repository,
                                 @Value("${xiangyun.auth.bootstrap.username:admin}") String username,
                                 @Value("${xiangyun.auth.bootstrap.password:}") String password,
                                 @Value("${xiangyun.auth.bootstrap.display-name:系统管理员}") String displayName,
                                 @Value("${xiangyun.auth.bootstrap.village-id:1}") String villageId) {
        this.repository = repository;
        this.username = username == null ? "" : username.trim();
        this.password = password;
        this.displayName = displayName == null ? "" : displayName.trim();
        this.villageId = villageId == null ? "" : villageId.trim();
    }

    @Override
    public void run(ApplicationArguments args) {
        validate();
        if (repository.existsByUsername(username)) {
            log.info("初始管理员账号 {} 已存在，跳过初始化且不覆盖现有数据", username);
            return;
        }
        try {
            AuthUserAccount account = repository.create(
                    username, displayName, "ADMIN", villageId, encoder.encode(password), true);
            log.info("已创建初始管理员账号 {} (id={})", account.username(), account.id());
        } catch (DuplicateKeyException ex) {
            log.info("初始管理员账号 {} 已由其他实例创建，跳过初始化", username);
        } catch (DataIntegrityViolationException ex) {
            if (repository.existsByUsername(username)) {
                log.info("初始管理员账号 {} 已由其他实例创建，跳过初始化", username);
                return;
            }
            throw ex;
        }
    }

    private void validate() {
        if (!StringUtils.hasText(username) || username.length() < 3 || username.length() > 64) {
            throw new IllegalStateException("AUTH_BOOTSTRAP_USERNAME 长度必须为 3 到 64 个字符");
        }
        if (!StringUtils.hasText(password) || password.length() < 12) {
            throw new IllegalStateException("启用管理员初始化时 AUTH_BOOTSTRAP_PASSWORD 至少需要 12 位");
        }
        if (!StringUtils.hasText(displayName) || displayName.length() > 64) {
            throw new IllegalStateException("AUTH_BOOTSTRAP_DISPLAY_NAME 不能为空且不能超过 64 个字符");
        }
        if (!StringUtils.hasText(villageId) || villageId.length() > 64) {
            throw new IllegalStateException("AUTH_BOOTSTRAP_VILLAGE_ID 不能为空且不能超过 64 个字符");
        }
    }
}
