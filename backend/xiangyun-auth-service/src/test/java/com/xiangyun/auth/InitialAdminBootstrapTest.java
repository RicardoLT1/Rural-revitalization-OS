package com.xiangyun.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InitialAdminBootstrapTest {

    @Test
    void createsAdminWithBcryptOnlyWhenAccountIsMissing() {
        AuthUserRepository repository = mock(AuthUserRepository.class);
        when(repository.existsByUsername("first_admin")).thenReturn(false);
        when(repository.create(eq("first_admin"), eq("首位管理员"), eq("ADMIN"), eq("1"), anyString(), eq(true)))
                .thenAnswer(invocation -> new AuthUserAccount(
                        "1", "first_admin", "首位管理员", "ADMIN", "1", invocation.getArgument(4), true));
        InitialAdminBootstrap bootstrap = new InitialAdminBootstrap(
                repository, "first_admin", "StrongPass2026!", "首位管理员", "1");

        bootstrap.run(new DefaultApplicationArguments(new String[0]));

        var hash = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(repository).create(eq("first_admin"), eq("首位管理员"), eq("ADMIN"), eq("1"), hash.capture(), eq(true));
        assertThat(new BCryptPasswordEncoder().matches("StrongPass2026!", hash.getValue())).isTrue();
    }

    @Test
    void existingAccountIsNeverOverwritten() {
        AuthUserRepository repository = mock(AuthUserRepository.class);
        when(repository.existsByUsername("first_admin")).thenReturn(true);
        InitialAdminBootstrap bootstrap = new InitialAdminBootstrap(
                repository, "first_admin", "StrongPass2026!", "首位管理员", "1");

        bootstrap.run(new DefaultApplicationArguments(new String[0]));

        verify(repository, never()).create(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    void enabledBootstrapRejectsWeakOrMissingPassword() {
        InitialAdminBootstrap bootstrap = new InitialAdminBootstrap(
                mock(AuthUserRepository.class), "first_admin", "short", "首位管理员", "1");

        assertThatThrownBy(() -> bootstrap.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("至少需要 12 位");
    }
}
