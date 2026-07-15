package com.xiangyun.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.BusinessException;
import com.xiangyun.common.dto.OperationStats;
import com.xiangyun.common.dto.PageResponse;
import com.xiangyun.common.dto.UserSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperationServiceTest {

    private JdbcTemplate jdbcTemplate;
    private StringRedisTemplate redisTemplate;
    private AuthClient authClient;
    private OutboxService outboxService;
    private OperationService service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        redisTemplate = mock(StringRedisTemplate.class);
        authClient = mock(AuthClient.class);
        outboxService = mock(OutboxService.class);
        service = new OperationService(jdbcTemplate, redisTemplate, authClient, new ObjectMapper(), outboxService, 600);
    }

    @Test
    void createResourceInsertsRecord() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
        Map<String, Object> result = service.createResource(Map.of("name", "新民宿"));
        assertThat(result.get("created")).isEqualTo(true);
    }

    @Test
    void updateResourceEvictsCache() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
        Map<String, Object> result = service.updateResource("101", Map.of("name", "更新"));
        assertThat(result.get("cacheEvicted")).isEqualTo(true);
        verify(redisTemplate).delete("resource:detail:101");
    }

    @Test
    void publishResourceUpdatesLifecycle() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        Map<String, Object> result = service.publishResource("101");

        assertThat(result.get("investmentStatus")).isEqualTo("可招商");
        verify(redisTemplate).delete("resource:detail:101");
    }

    @Test
    void offlineResourceUpdatesLifecycle() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        Map<String, Object> result = service.offlineResource("101");

        assertThat(result.get("investmentStatus")).isEqualTo("已下架");
        verify(redisTemplate).delete("resource:detail:101");
    }

    @Test
    void deleteResourceMarksDeleted() {
        when(jdbcTemplate.update(anyString(), eq(101L))).thenReturn(1);
        Map<String, Object> result = service.deleteResource("101");
        assertThat(result.get("deleted")).isEqualTo(true);
    }

    @Test
    void createWeeklyReportPersistsConfirmedContent() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        Map<String, Object> result = service.createWeeklyReport(Map.of(
                "weekStart", "2026-07-06",
                "weekEnd", "2026-07-12",
                "title", "第 28 周运营周报",
                "summary", "本周资源与审批运行稳定"
        ), "2", "业务工作人员", "1");

        assertThat(result).containsEntry("saved", true).containsEntry("status", "PUBLISHED");
        verify(jdbcTemplate).update(startsWith("insert into weekly_report"), any(Object[].class));
    }

    @Test
    void createWeeklyReportRejectsMissingSummary() {
        assertThatThrownBy(() -> service.createWeeklyReport(Map.of("title", "周报"), "2", "工作人员", "1"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void statsCombinesOperationCounts() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(3, 2, 4, 5, 1);
        OperationStats stats = service.stats();
        assertThat(stats.resourceCount()).isEqualTo(3);
        assertThat(stats.riskWorkflowCount()).isEqualTo(1);
        verify(jdbcTemplate).queryForObject(contains("due_date < now()"), eq(Integer.class));
    }

    @Test
    void resourcePageReturnsServerPaginationMetadata() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(17);
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), any(Object[].class)))
                .thenReturn(List.of());

        PageResponse<ResourceView> page = service.resourcePage("土地", "可招商", "青耘", 2, 5);

        assertThat(page.page()).isEqualTo(2);
        assertThat(page.pageSize()).isEqualTo(5);
        assertThat(page.total()).isEqualTo(17);
        assertThat(page.totalPages()).isEqualTo(4);
    }

    @Test
    void workbenchReturnsStatsAndLists() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(), List.of());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(3, 2, 4, 5, 1);
        Map<String, Object> result = service.workbench("全部");
        assertThat(result).containsKeys("todoStats", "approvals", "filteredTodos");
    }

    /*
    @Test
    void approveWritesApprovalRecordLegacy() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
        Map<String, Object> result = service.approve("201", "pass", Map.of("remark", "同意"));
        assertThat(result.get("saved")).isEqualTo(true);
    }

    */

    @Test
    void approveWritesApprovalRecord() {
        when(jdbcTemplate.queryForMap(anyString(), eq(201L))).thenReturn(Map.of("id", 201, "title", "合作申请", "status", "PENDING"));
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        Map<String, Object> result = service.approve("201", "pass", "2", Map.of("remark", "同意"));

        assertThat(result.get("saved")).isEqualTo(true);
        assertThat(result.get("status")).isEqualTo("APPROVED");
        verify(jdbcTemplate).update(startsWith("insert into approval_record"), any(Object[].class));
        verify(jdbcTemplate).update(startsWith("update workflow"), any(Object[].class));
        verify(jdbcTemplate).update(startsWith("update todo_item"), any(Object[].class));
    }

    @Test
    void approveRejectsAlreadyHandledWorkflow() {
        when(jdbcTemplate.queryForMap(anyString(), eq(201L))).thenReturn(Map.of("id", 201, "title", "合作申请", "status", "APPROVED"));

        assertThatThrownBy(() -> service.approve("201", "pass", "2", Map.of()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void submitCooperationApplicationCreatesWorkflowAndTodo() {
        ResourceView resource = new ResourceView("101", "溪畔民宿院", "闲置农房",
                null, null, "青耘村", null, null, "可招商",
                List.of(), "介绍", "村公司", "0572", List.of(), 0, 0,
                "村集体确认", "基础材料齐全", List.of(), "适合民宿合作");
        when(jdbcTemplate.query(startsWith("select * from resource"), any(org.springframework.jdbc.core.RowMapper.class), eq(101L)))
                .thenReturn(List.of(resource));
        when(jdbcTemplate.queryForObject(startsWith("select count(*) from workflow"), eq(Integer.class), any(Object[].class)))
                .thenReturn(0);
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        Map<String, Object> result = service.submitCooperationApplication("1", Map.of("resourceId", "101"));

        assertThat(result.get("created")).isEqualTo(true);
        assertThat(result.get("status")).isEqualTo("PENDING");
        verify(jdbcTemplate).update(startsWith("insert into workflow"), any(Object[].class));
        verify(jdbcTemplate).update(startsWith("insert into todo_item"), any(Object[].class));
    }

    @Test
    void submitCooperationApplicationRejectsDuplicatePendingRequest() {
        ResourceView resource = new ResourceView("101", "溪畔民宿院", "闲置农房",
                null, null, "青耘村", null, null, "可招商",
                List.of(), "介绍", "村公司", "0572", List.of(), 0, 0,
                "村集体确认", "基础材料齐全", List.of(), "适合民宿合作");
        when(jdbcTemplate.query(startsWith("select * from resource"), any(org.springframework.jdbc.core.RowMapper.class), eq(101L)))
                .thenReturn(List.of(resource));
        when(jdbcTemplate.queryForObject(startsWith("select count(*) from workflow"), eq(Integer.class), any(Object[].class)))
                .thenReturn(1);

        assertThatThrownBy(() -> service.submitCooperationApplication("1", Map.of("resourceId", "101")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void workflowUsesAuthFeignSummary() {
        when(jdbcTemplate.queryForMap(anyString(), eq(201L))).thenReturn(Map.of("id", 201, "title", "流程", "status", "进行中", "current_node_id", "review"));
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(201L))).thenReturn(List.of());
        when(authClient.userSummary("2")).thenReturn(ApiResponse.success(new UserSummary("2", "operator", "乡村运营员", "operator", "1")));
        WorkflowView view = service.workflow("201");
        assertThat(view.applicantName()).isEqualTo("乡村运营员");
    }
}
