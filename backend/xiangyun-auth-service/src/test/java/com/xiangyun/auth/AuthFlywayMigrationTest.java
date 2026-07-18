package com.xiangyun.auth;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthFlywayMigrationTest {

    @Test
    void baseMigrationCreatesEmptyProductionDirectory() {
        DriverManagerDataSource dataSource = dataSource();

        migrate(dataSource, "classpath:db/migration");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        assertThat(jdbcTemplate.queryForObject("select count(*) from auth_user", Integer.class)).isZero();
    }

    @Test
    void demoMigrationSeedsSevenWorkingBcryptAccounts() {
        DriverManagerDataSource dataSource = dataSource();

        migrate(dataSource, "classpath:db/migration", "classpath:db/demo");

        AuthUserRepository repository = new AuthUserRepository(new JdbcTemplate(dataSource));
        var accounts = repository.findAll();
        assertThat(accounts).hasSize(7);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertThat(accounts).allMatch(account -> encoder.matches("123456", account.passwordHash()));
        assertThat(repository.findByUsername("admin")).get()
                .extracting(AuthUserAccount::role, AuthUserAccount::enabled)
                .containsExactly("ADMIN", true);
        assertThat(repository.findByUsername("disabled")).get()
                .extracting(AuthUserAccount::enabled)
                .isEqualTo(false);
    }

    private DriverManagerDataSource dataSource() {
        return new DriverManagerDataSource(
                "jdbc:h2:mem:auth-migration-" + UUID.randomUUID()
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "sa", "");
    }

    private void migrate(DriverManagerDataSource dataSource, String... locations) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .table("flyway_auth_history")
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("0"))
                .load()
                .migrate();
    }
}
