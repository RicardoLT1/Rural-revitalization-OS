package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private JdbcTemplate jdbcTemplate;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        service = new NotificationService(jdbcTemplate, mock(SystemSettingsService.class));
    }

    @Test
    void markReadPersistsPerUserState() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(8L), eq(1L), eq("STAFF")))
                .thenReturn(1);

        Map<String, Object> result = service.markRead("8", "1", "2", "STAFF");

        assertThat(result).containsEntry("read", true);
        verify(jdbcTemplate).update("insert ignore into admin_notification_read(notification_id,user_id) values(?,?)", 8L, "2");
    }

    @Test
    void normalUserCannotReadAdminNotifications() {
        assertThatThrownBy(() -> service.markAllRead("1", "1", "USER"))
                .isInstanceOf(BusinessException.class);
    }
}
