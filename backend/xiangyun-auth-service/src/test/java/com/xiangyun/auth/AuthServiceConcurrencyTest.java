package com.xiangyun.auth;

import com.xiangyun.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceConcurrencyTest {

    @Test
    void mapsConcurrentUsernameConflictToBusinessConflict() {
        AuthUserRepository repository = mock(AuthUserRepository.class);
        when(repository.existsByUsername("racing_user")).thenReturn(false);
        when(repository.create(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenThrow(new DuplicateKeyException("duplicate username"));
        AuthService service = new AuthService(repository, mock(StringRedisTemplate.class), "test-secret", 7200);

        Throwable thrown = catchThrowable(() -> service.register("racing_user", "123456", "并发用户"));

        assertThat(thrown).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) thrown).getCode()).isEqualTo(40900);
    }
}
