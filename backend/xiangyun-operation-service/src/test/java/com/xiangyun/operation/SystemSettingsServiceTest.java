package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemSettingsServiceTest {

    private JdbcTemplate jdbcTemplate;
    private SystemSettingsService service;
    private SystemSettingsView settings;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        service = new SystemSettingsService(jdbcTemplate);
        settings = new SystemSettingsView("1", "乡耘 OS", "青耘村",
                new BigDecimal("30.640522"), new BigDecimal("119.681337"),
                24, 1, true, true, "0572", "admin", LocalDateTime.now(), "v1.3-admin-pro");
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), any(Object[].class)))
                .thenReturn(List.of(settings));
    }

    @Test
    void updatePersistsVillageRulesAndName() {
        SystemSettingsView result = service.update("1", "admin", Map.of(
                "platformName", "乡耘运营平台",
                "villageName", "青耘村",
                "approvalTimeoutHours", 36));

        assertThat(result.villageId()).isEqualTo("1");
        verify(jdbcTemplate).update(org.mockito.ArgumentMatchers.startsWith("update system_setting"), any(Object[].class));
        verify(jdbcTemplate).update("update village set name=? where id=? and deleted=0", "青耘村", 1L);
    }

    @Test
    void updateRejectsInvalidApprovalThreshold() {
        assertThatThrownBy(() -> service.update("1", "admin", Map.of("approvalTimeoutHours", 200)))
                .isInstanceOf(BusinessException.class);
    }
}
