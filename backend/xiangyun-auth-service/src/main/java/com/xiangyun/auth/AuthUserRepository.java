package com.xiangyun.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class AuthUserRepository {

    private static final String SELECT_COLUMNS = """
            select id,username,display_name,role_code,village_id,password_hash,enabled
            from auth_user
            """;

    private final JdbcTemplate jdbcTemplate;

    public AuthUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AuthUserAccount> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return jdbcTemplate.query(SELECT_COLUMNS + " where username=?", this::map, username)
                .stream().findFirst();
    }

    public Optional<AuthUserAccount> findById(String id) {
        Long numericId = parseId(id);
        if (numericId == null) return Optional.empty();
        return jdbcTemplate.query(SELECT_COLUMNS + " where id=?", this::map, numericId)
                .stream().findFirst();
    }

    public List<AuthUserAccount> findAll() {
        return jdbcTemplate.query(SELECT_COLUMNS + " order by id", this::map);
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from auth_user where username=?", Integer.class, username);
        return count != null && count > 0;
    }

    public AuthUserAccount create(String username,
                                  String displayName,
                                  String role,
                                  String villageId,
                                  String passwordHash,
                                  boolean enabled) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into auth_user(username,display_name,role_code,village_id,password_hash,enabled)
                    values(?,?,?,?,?,?)
                    """, new String[]{"id"});
            statement.setString(1, username);
            statement.setString(2, displayName);
            statement.setString(3, role);
            statement.setString(4, villageId);
            statement.setString(5, passwordHash);
            statement.setBoolean(6, enabled);
            return statement;
        }, keyHolder);
        Number id = keyHolder.getKey();
        if (id == null) throw new IllegalStateException("创建账号后未返回主键");
        return findById(String.valueOf(id.longValue()))
                .orElseThrow(() -> new IllegalStateException("创建账号后无法读取账号"));
    }

    public void updateDisplayName(String id, String displayName) {
        jdbcTemplate.update("""
                update auth_user set display_name=?,updated_at=current_timestamp where id=?
                """, displayName, id);
    }

    public void updatePasswordHash(String id, String passwordHash) {
        jdbcTemplate.update("""
                update auth_user set password_hash=?,updated_at=current_timestamp where id=?
                """, passwordHash, id);
    }

    public void updateAccount(String id,
                              String displayName,
                              String role,
                              String villageId,
                              boolean enabled) {
        jdbcTemplate.update("""
                update auth_user
                set display_name=?,role_code=?,village_id=?,enabled=?,updated_at=current_timestamp
                where id=?
                """, displayName, role, villageId, enabled, id);
    }

    public void updateEnabled(String id, boolean enabled) {
        jdbcTemplate.update("""
                update auth_user set enabled=?,updated_at=current_timestamp where id=?
                """, enabled, id);
    }

    public void updateRole(String id, String role) {
        jdbcTemplate.update("""
                update auth_user set role_code=?,updated_at=current_timestamp where id=?
                """, role, id);
    }

    private AuthUserAccount map(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new AuthUserAccount(
                String.valueOf(resultSet.getLong("id")),
                resultSet.getString("username"),
                resultSet.getString("display_name"),
                resultSet.getString("role_code"),
                resultSet.getString("village_id"),
                resultSet.getString("password_hash"),
                resultSet.getBoolean("enabled")
        );
    }

    private Long parseId(String id) {
        try {
            return id == null ? null : Long.parseLong(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
