package com.xiangyun.analysis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.dto.AdminOperationOverview;
import com.xiangyun.common.dto.ResourceSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);
    private static final Duration LAST_SUCCESS_TTL = Duration.ofHours(24);

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final OperationClient operationClient;
    private final ObjectMapper objectMapper;
    private final ReportProperties reportProperties;
    private final long dashboardTtlSeconds;

    public AnalysisService(JdbcTemplate jdbcTemplate,
                           StringRedisTemplate redisTemplate,
                           OperationClient operationClient,
                           ObjectMapper objectMapper,
                           ReportProperties reportProperties,
                           @org.springframework.beans.factory.annotation.Value("${xiangyun.cache.dashboard-ttl-seconds}") long dashboardTtlSeconds) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.operationClient = operationClient;
        this.objectMapper = objectMapper;
        this.reportProperties = reportProperties;
        this.dashboardTtlSeconds = dashboardTtlSeconds;
    }

    public DashboardResult dashboard(String villageId, Integer days) {
        int range = resolveRange(days);
        String cacheKey = dashboardCacheKey(villageId, range);
        Map<String, Object> cached = readCachedData(cacheKey);
        if (cached != null) {
            log.info("dashboard cache hit");
            return new DashboardResult(cached, "HIT", false);
        }

        log.info("dashboard cache miss");
        try {
            Map<String, Object> result = buildDashboard(villageId, range);
            writeDashboardCache(cacheKey, result, range);
            log.info("dashboard cache write");
            return new DashboardResult(result, "MISS", false);
        } catch (Exception ex) {
            Map<String, Object> stale = readCachedData(lastSuccessKey(cacheKey));
            if (stale != null) {
                log.warn("dashboard stale cache returned after operation failure");
                return new DashboardResult(stale, "STALE", true);
            }
            throw new DashboardUnavailableException("统计服务暂时不可用，请稍后重试");
        }
    }

    public DashboardResult refreshDashboard(String villageId, Integer days) {
        int range = resolveRange(days);
        redisTemplate.delete(dashboardCacheKey(villageId, range));
        return dashboard(villageId, range);
    }

    public Map<String, Object> reportDashboard(String period) {
        int range = "30d".equals(period) ? 30 : 7;
        List<Map<String, Object>> snapshots = snapshots(range);
        BigDecimal revenue = snapshots.stream()
                .map(item -> (BigDecimal) item.get("revenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int visitors = snapshots.stream().mapToInt(item -> ((Number) item.get("visitor_count")).intValue()).sum();
        int applicationCount = safeCount("select count(*) from workflow where deleted=0 and category='COOPERATION_APPLICATION'");
        int approvedCount = safeCount("select count(*) from workflow where deleted=0 and category='COOPERATION_APPLICATION' and status='APPROVED'");
        int overdueTodoCount = safeCount("select count(*) from todo_item where deleted=0 and due_date < now() and status='PENDING'");
        String approvalRate = applicationCount == 0
                ? "0%"
                : BigDecimal.valueOf(approvedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(applicationCount), 1, RoundingMode.HALF_UP) + "%";
        return Map.of(
                "summary", List.of(
                        Map.of("id", "visitor", "title", "累计客流", "value", visitors, "delta", "+8.6%"),
                        Map.of("id", "revenue", "title", "累计营收", "value", revenue.setScale(1, RoundingMode.HALF_UP) + "万元", "delta", "+12.3%"),
                        Map.of("id", "applications", "title", "合作申请", "value", applicationCount, "delta", "闭环跟进"),
                        Map.of("id", "approvalRate", "title", "审批通过率", "value", approvalRate, "delta", "已处理 " + approvedCount),
                        Map.of("id", "overdueTodos", "title", "超时待办", "value", overdueTodoCount, "delta", overdueTodoCount == 0 ? "无超时" : "需跟进")
                ),
                "period", range == 30 ? "30d" : "7d",
                "flowPoints", trend(range),
                "revenueBar", Map.of("labels", snapshots.stream().map(item -> String.valueOf(item.get("stat_date"))).toList(), "series", List.of(Map.of("name", "营收", "values", snapshots.stream().map(item -> item.get("revenue")).toList()))),
                "ratioRing", Map.of("labels", List.of("文旅收入", "农产品销售", "活动服务"), "values", List.of(55, 27, 18), "colors", List.of("#2F7D32", "#6FAF5E", "#D58A2A")),
                "autoSummary", "系统自动汇总：当前周期客流与营收保持上行，可继续推进重点资源招商。",
                "aiTips", List.of(Map.of("id", "tip-1", "title", "提升转化", "content", "将热门资源与活动套餐联动招商。", "priority", "P1"))
        );
    }

    public List<Map<String, Object>> forecast() {
        return jdbcTemplate.queryForList("select forecast_date as date, actual_value as actual, predict_value as predict, upper_value as upper, lower_value as lower, risk_level as riskLevel, strategy from forecast_result where deleted=0 order by forecast_date");
    }

    public Map<String, Object> investmentMatch(String resourceId) {
        ResourceSummary resource = operationClient.resourceSummary(resourceId).data();
        List<Map<String, Object>> matches = jdbcTemplate.queryForList("select investor,score,reason,priority,direction from investment_match_record where resource_id=? and deleted=0 order by score desc", Long.parseLong(resourceId));
        return Map.of("resource", resource, "matches", matches, "aiSummary", Map.of("title", "招商策略建议", "priority", "P1", "content", "优先对接高评分对象并安排现场踏勘。"));
    }

    private Map<String, Object> buildDashboard(String villageId, int range) {
        AdminOperationOverview ops = operationClient.adminOverview(villageId).data();
        List<Map<String, Object>> snapshots = snapshots(range);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("villageName", ops.villageName());
        result.put("roleName", "乡村运营管理平台");
        result.put("generatedAt", Instant.now().toString());
        result.put("rangeDays", range);
        result.put("stats", List.of(
                metric("resource", "资源总数", ops.resourceCount(), "个", "今日更新", ops.resourcesUpdatedToday(), ""),
                metric("ready", "可合作资源", ops.investmentReadyCount(), "个", "今日更新", ops.readyResourcesUpdatedToday(), ""),
                metric("todo", "待审批数", ops.pendingApprovalCount(), "项", "今日新增", ops.pendingCreatedToday(), "今日已处理 " + ops.workflowsResolvedToday() + " 项"),
                metric("risk", "超时流程", ops.overdueApprovalCount(), "项", "实时超时", ops.overdueApprovalCount(), "按截止时间实时计算")
        ));
        result.put("resourceDistribution", resourceDistribution(ops));
        result.put("trends", Map.of("days7", trend(7), "days30", trend(30)));
        result.put("risks", dashboardRisks(ops));
        result.put("suggestions", dashboardSuggestions(ops));
        result.put("snapshotCount", snapshots.size());
        return result;
    }

    private Map<String, Object> metric(String key,
                                       String title,
                                       int value,
                                       String unit,
                                       String changeLabel,
                                       int changeValue,
                                       String changeHint) {
        return Map.of(
                "key", key,
                "title", title,
                "value", value,
                "unit", unit,
                "trend", changeValue > 0 ? "up" : "flat",
                "changeLabel", changeLabel,
                "changeValue", changeValue,
                "changeHint", changeHint
        );
    }

    private List<Map<String, Object>> resourceDistribution(AdminOperationOverview overview) {
        int total = Math.max(overview.resourceCount(), 1);
        return overview.resourceDistribution().stream()
                .map(item -> Map.<String, Object>of(
                        "label", item.category(),
                        "count", item.count(),
                        "percentage", BigDecimal.valueOf(item.count() * 100.0 / total)
                                .setScale(1, RoundingMode.HALF_UP)))
                .toList();
    }

    private List<Map<String, Object>> dashboardRisks(AdminOperationOverview overview) {
        if (overview.overdueApprovalCount() == 0) {
            return List.of();
        }
        return List.of(Map.of(
                "id", "overdue-approval",
                "title", "审批流程已超时",
                "level", "high",
                "detail", "当前有 " + overview.overdueApprovalCount() + " 条待办超过截止时间，请尽快核查。",
                "assignee", "审批工作台"));
    }

    private List<Map<String, Object>> dashboardSuggestions(AdminOperationOverview overview) {
        if (overview.overdueApprovalCount() > 0) {
            return List.of(Map.of(
                    "id", "handle-overdue",
                    "title", "优先处理超时审批",
                    "content", "先处理 " + overview.overdueApprovalCount() + " 条超时流程，降低业务等待风险。",
                    "priority", "P0",
                    "actionLabel", "查看超时流程",
                    "actionType", "VIEW_RISK",
                    "tag", "审批"));
        }
        if (overview.investmentReadyCount() > 0) {
            return List.of(Map.of(
                    "id", "operate-ready-resource",
                    "title", "推进可合作资源运营",
                    "content", "当前有 " + overview.investmentReadyCount() + " 个可合作资源，可优先完善材料并开展招商跟进。",
                    "priority", "P1",
                    "actionLabel", "查看可合作资源",
                    "actionType", "VIEW_RESOURCES",
                    "tag", "资源"));
        }
        return List.of(Map.of(
                "id", "complete-resource",
                "title", "完善资源档案",
                "content", "暂未发现可合作资源，建议先完善权属与招商材料。",
                "priority", "P1",
                "actionLabel", "进入资源目录",
                "actionType", "VIEW_RESOURCES",
                "tag", "资源"));
    }

    private void writeDashboardCache(String cacheKey, Map<String, Object> result, int range) {
        try {
            Map<String, Object> envelope = Map.of(
                    "data", result,
                    "generatedAt", Instant.now().toString(),
                    "rangeDays", range
            );
            String payload = objectMapper.writeValueAsString(envelope);
            redisTemplate.opsForValue().set(cacheKey, payload, Duration.ofSeconds(dashboardTtlSeconds));
            redisTemplate.opsForValue().set(lastSuccessKey(cacheKey), payload, LAST_SUCCESS_TTL);
        } catch (Exception ignored) {
        }
    }

    private Map<String, Object> readCachedData(String cacheKey) {
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null) {
                return null;
            }
            Map<String, Object> value = objectMapper.readValue(cached, new TypeReference<>() {
            });
            Object data = value.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                return (Map<String, Object>) dataMap;
            }
            return value;
        } catch (Exception ignored) {
            return null;
        }
    }

    private int resolveRange(Integer days) {
        return days == null ? reportProperties.getDefaultRangeDays() : days;
    }

    private String dashboardCacheKey(String villageId, int range) {
        return "analysis:dashboard:v2:" + villageId + ":" + range;
    }

    private String lastSuccessKey(String cacheKey) {
        return cacheKey + ":last-success";
    }

    private List<Map<String, Object>> snapshots(int range) {
        return jdbcTemplate.queryForList("select * from report_snapshot where deleted=0 order by stat_date desc limit ?", range)
                .stream().sorted((a, b) -> String.valueOf(a.get("stat_date")).compareTo(String.valueOf(b.get("stat_date")))).toList();
    }

    private List<Map<String, Object>> trend(int range) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        return snapshots(range).stream()
                .map(item -> Map.of("date", formatter.format(((java.sql.Date) item.get("stat_date")).toLocalDate()), "value", item.get("visitor_count")))
                .toList();
    }

    private int safeCount(String sql) {
        try {
            Integer value = jdbcTemplate.queryForObject(sql, Integer.class);
            return value == null ? 0 : value;
        } catch (Exception ex) {
            return 0;
        }
    }
}
