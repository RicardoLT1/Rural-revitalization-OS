package com.xiangyun.operation;

import com.xiangyun.common.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAuditServiceTest {

    @Test
    void recordsAndPagesAuditEvents() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        AdminAuditService service = new AdminAuditService(jdbcTemplate);
        AdminAuditEvent event = new AdminAuditEvent(
                "trace-1", "3", "admin", "ADMIN", "1", "RESOURCE", "PUBLISH_RESOURCE",
                "RESOURCE", "101", "POST", "/api/resources/101/publish", "127.0.0.1", "test",
                "SUCCESS", 200, "耗时 2ms", "{\"status\":\"draft\"}", "{\"status\":\"active\"}");
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of(Map.of("id", 1L, "action", "PUBLISH_RESOURCE")));

        service.record(event);
        PageResponse<Map<String, Object>> page = service.page(
                "trace-1", "RESOURCE", "SUCCESS", null, null, 1, 20);

        verify(jdbcTemplate).update(anyString(), any(Object[].class));
        assertThat(page.total()).isEqualTo(1);
        assertThat(page.items()).hasSize(1);
    }
}
