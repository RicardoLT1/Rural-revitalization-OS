package com.xiangyun.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.dto.AdminOperationOverview;
import com.xiangyun.common.dto.ResourceSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalysisServiceTest {

    private JdbcTemplate jdbcTemplate;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private OperationClient operationClient;
    private AnalysisService service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        operationClient = mock(OperationClient.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ReportProperties reportProperties = new ReportProperties();
        ReflectionTestUtils.setField(reportProperties, "defaultRangeDays", 7);
        service = new AnalysisService(jdbcTemplate, redisTemplate, operationClient, new ObjectMapper(), reportProperties, 180);
    }

    @Test
    void dashboardUsesRedisCacheWhenPresent() {
        when(valueOperations.get("analysis:dashboard:v2:1:7")).thenReturn("{\"data\":{\"villageName\":\"demo\"},\"rangeDays\":7}");
        DashboardResult result = service.dashboard("1", 7);
        assertThat(result.cacheStatus()).isEqualTo("HIT");
        assertThat(result.data()).containsKey("villageName");
    }

    @Test
    void dashboardCombinesFeignAndDatabaseData() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(operationClient.adminOverview("1")).thenReturn(ApiResponse.success(overview()));
        when(jdbcTemplate.queryForList(anyString(), eq(7))).thenReturn(snapshotRows());
        when(jdbcTemplate.queryForList(anyString(), eq(30))).thenReturn(snapshotRows());
        DashboardResult result = service.dashboard("1", 7);
        assertThat(result.cacheStatus()).isEqualTo("MISS");
        assertThat(result.data()).doesNotContainKey("cacheHit");
        assertThat(result.data()).containsEntry("villageName", "青耘村");
        assertThat((List<?>) result.data().get("resourceDistribution")).hasSize(2);
        verify(valueOperations, atLeastOnce()).set(anyString(), anyString(), any());
    }

    @Test
    void dashboardReturnsStaleCacheWhenOperationFails() {
        when(valueOperations.get("analysis:dashboard:v2:1:7")).thenReturn(null);
        when(valueOperations.get("analysis:dashboard:v2:1:7:last-success")).thenReturn("{\"data\":{\"villageName\":\"old\"},\"rangeDays\":7}");
        when(operationClient.adminOverview("1")).thenThrow(new RuntimeException("operation down"));

        DashboardResult result = service.dashboard("1", 7);

        assertThat(result.cacheStatus()).isEqualTo("STALE");
        assertThat(result.stale()).isTrue();
        assertThat(result.data()).containsEntry("villageName", "old");
    }

    @Test
    void reportDashboardSortsTrendData() {
        when(jdbcTemplate.queryForList(anyString(), eq(7))).thenReturn(snapshotRows());
        Map<String, Object> result = service.reportDashboard("7d");
        assertThat(result).containsKeys("summary", "flowPoints", "revenueBar");
    }

    @Test
    void investmentMatchCombinesFeignResource() {
        when(operationClient.resourceSummary("103")).thenReturn(ApiResponse.success(new ResourceSummary("103", "老粮仓文创空间", "文旅空间", BigDecimal.TEN, BigDecimal.TEN, "可招商", List.of("文旅空间"))));
        when(jdbcTemplate.queryForList(anyString(), eq(103L))).thenReturn(List.of(Map.of("investor", "A", "score", 94)));
        Map<String, Object> result = service.investmentMatch("103");
        assertThat(result).containsKeys("resource", "matches", "aiSummary");
    }

    @Test
    void forecastReturnsRows() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(Map.of("date", "06-27", "predict", 2500)));
        assertThat(service.forecast()).hasSize(1);
    }

    private List<Map<String, Object>> snapshotRows() {
        return List.of(
                Map.of("stat_date", Date.valueOf("2026-06-25"), "visitor_count", 100, "revenue", BigDecimal.TEN),
                Map.of("stat_date", Date.valueOf("2026-06-26"), "visitor_count", 120, "revenue", BigDecimal.valueOf(12))
        );
    }

    private AdminOperationOverview overview() {
        return new AdminOperationOverview(
                "1",
                "青耘村",
                3,
                2,
                5,
                1,
                1,
                1,
                2,
                1,
                List.of(
                        new AdminOperationOverview.ResourceCategoryCount("乡村民宿", 2),
                        new AdminOperationOverview.ResourceCategoryCount("土地", 1)));
    }
}
