package com.xiangyun.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.BusinessException;
import com.xiangyun.common.dto.OperationStats;
import com.xiangyun.common.dto.ResourceSummary;
import com.xiangyun.common.dto.UserSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class OperationService {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final AuthClient authClient;
    private final ObjectMapper objectMapper;
    private final long resourceDetailTtlSeconds;

    public OperationService(JdbcTemplate jdbcTemplate,
                            StringRedisTemplate redisTemplate,
                            AuthClient authClient,
                            ObjectMapper objectMapper,
                            @Value("${xiangyun.cache.resource-detail-ttl-seconds}") long resourceDetailTtlSeconds) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.authClient = authClient;
        this.objectMapper = objectMapper;
        this.resourceDetailTtlSeconds = resourceDetailTtlSeconds;
    }

    public List<Map<String, Object>> villages() {
        return jdbcTemplate.queryForList("select id, name, region, address, status from village where deleted=0 order by id");
    }

    public List<ResourceView> resources(String category, String investmentStatus, String keyword) {
        return resources(category, investmentStatus, keyword, null, null);
    }

    public List<ResourceView> resources(String category, String investmentStatus, String keyword, Integer page, Integer size) {
        String sql = "select * from resource where deleted=0";
        if (StringUtils.hasText(category)) {
            sql += " and category='" + category.replace("'", "") + "'";
        }
        if (StringUtils.hasText(investmentStatus)) {
            sql += " and investment_status='" + investmentStatus.replace("'", "") + "'";
        }
        if (StringUtils.hasText(keyword)) {
            sql += " and name like '%" + keyword.replace("'", "") + "%'";
        }
        sql += " order by annual_estimate desc, id asc";
        if (page != null && size != null) {
            int actualSize = Math.max(1, Math.min(size, 50));
            int actualPage = Math.max(1, page);
            int offset = (actualPage - 1) * actualSize;
            sql += " limit " + actualSize + " offset " + offset;
        }
        return jdbcTemplate.query(sql, (rs, rowNum) -> toResource(rs.getLong("id"), rs.getString("name"), rs.getString("category"),
                rs.getBigDecimal("lat"), rs.getBigDecimal("lng"), rs.getString("address"), rs.getBigDecimal("area"),
                rs.getBigDecimal("annual_estimate"), rs.getString("investment_status"), rs.getString("intro"),
                rs.getString("owner"), rs.getString("contact"), rs.getString("related_projects"),
                rs.getInt("occupancy_rate"), rs.getInt("expected_roi")));
    }

    public ResourceView detail(String id) {
        String cacheKey = "resource:detail:" + id;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, ResourceView.class);
            }
        } catch (Exception ignored) {
        }
        List<ResourceView> list = jdbcTemplate.query("select * from resource where id=? and deleted=0",
                (rs, rowNum) -> toResource(rs.getLong("id"), rs.getString("name"), rs.getString("category"),
                        rs.getBigDecimal("lat"), rs.getBigDecimal("lng"), rs.getString("address"), rs.getBigDecimal("area"),
                        rs.getBigDecimal("annual_estimate"), rs.getString("investment_status"), rs.getString("intro"),
                        rs.getString("owner"), rs.getString("contact"), rs.getString("related_projects"),
                        rs.getInt("occupancy_rate"), rs.getInt("expected_roi")), Long.parseLong(id));
        if (list.isEmpty()) {
            throw new BusinessException(40400, "资源不存在");
        }
        ResourceView view = list.get(0);
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(view), Duration.ofSeconds(resourceDetailTtlSeconds));
        } catch (Exception ignored) {
        }
        return view;
    }

    public Map<String, Object> createResource(Map<String, Object> body) {
        long id = nextId();
        jdbcTemplate.update("""
                insert into resource(id, village_id, name, category, lat, lng, address, area, annual_estimate, investment_status, intro, owner, contact, status)
                values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                id, 1, body.getOrDefault("name", "新资源"), body.getOrDefault("category", "闲置农房"),
                body.getOrDefault("lat", 30.638211), body.getOrDefault("lng", 119.684912),
                body.getOrDefault("address", "青耘村"), body.getOrDefault("area", 100),
                body.getOrDefault("annualEstimate", 10), body.getOrDefault("investmentStatus", "可招商"),
                body.getOrDefault("intro", "新增资源"), body.getOrDefault("owner", "青耘村运营公司"),
                body.getOrDefault("contact", "0572-8001000"), "active");
        return Map.of("id", String.valueOf(id), "created", true);
    }

    public Map<String, Object> updateResource(String id, Map<String, Object> body) {
        jdbcTemplate.update("""
                update resource
                set name=coalesce(?, name),
                    category=coalesce(?, category),
                    address=coalesce(?, address),
                    area=coalesce(?, area),
                    annual_estimate=coalesce(?, annual_estimate),
                    investment_status=coalesce(?, investment_status),
                    intro=coalesce(?, intro),
                    owner=coalesce(?, owner),
                    contact=coalesce(?, contact),
                    status=coalesce(?, status),
                    updated_at=now()
                where id=? and deleted=0
                """,
                body.get("name"),
                body.get("category"),
                body.get("address"),
                body.get("area"),
                body.get("annualEstimate"),
                body.get("investmentStatus"),
                body.get("intro"),
                body.get("owner"),
                body.get("contact"),
                body.get("status"),
                Long.parseLong(id));
        redisTemplate.delete("resource:detail:" + id);
        return Map.of("id", id, "updated", true, "cacheEvicted", true);
    }

    public Map<String, Object> deleteResource(String id) {
        jdbcTemplate.update("update resource set deleted=1, updated_at=now() where id=?", Long.parseLong(id));
        redisTemplate.delete("resource:detail:" + id);
        return Map.of("id", id, "deleted", true);
    }

    public Map<String, Object> publishResource(String id) {
        return updateResourceLifecycle(id, "active", "可招商");
    }

    public Map<String, Object> offlineResource(String id) {
        return updateResourceLifecycle(id, "offline", "已下架");
    }

    public Map<String, Object> updateInvestmentStatus(String id, String investmentStatus) {
        if (!List.of("可招商", "洽谈中", "已签约", "已下架").contains(investmentStatus)) {
            throw new BusinessException(40004, "招商状态不合法");
        }
        int updated = jdbcTemplate.update("update resource set investment_status=?, updated_at=now() where id=? and deleted=0",
                investmentStatus, Long.parseLong(id));
        if (updated == 0) {
            throw new BusinessException(40400, "资源不存在");
        }
        redisTemplate.delete("resource:detail:" + id);
        return Map.of("id", id, "investmentStatus", investmentStatus, "updated", true);
    }

    public Map<String, Object> resourceApplicationCount(String id) {
        Integer total = jdbcTemplate.queryForObject("""
                select count(*) from workflow
                where deleted=0 and category='COOPERATION_APPLICATION' and resource_id=?
                """, Integer.class, Long.parseLong(id));
        return Map.of("resourceId", id, "applicationCount", total == null ? 0 : total);
    }

    public List<String> tags() {
        return jdbcTemplate.queryForList("select name from resource_tag where deleted=0 order by sort_no, id", String.class);
    }

    public WorkflowView workflow(String id) {
        Map<String, Object> wf = jdbcTemplate.queryForMap("select * from workflow where id=? and deleted=0", Long.parseLong(id));
        String applicantName = String.valueOf(wf.getOrDefault("applicant_name", ""));
        if (!StringUtils.hasText(applicantName) || "null".equals(applicantName)) {
            Object applicantUserId = wf.get("applicant_user_id");
            String applicantId = applicantUserId == null ? String.valueOf(wf.getOrDefault("applicant", "2")) : String.valueOf(applicantUserId);
            UserSummary applicant = authClient.userSummary(applicantId).data();
            applicantName = applicant.displayName();
        }
        List<WorkflowView.Node> nodes = jdbcTemplate.query("select * from workflow_node where workflow_id=? and deleted=0 order by sort_no",
                (rs, rowNum) -> new WorkflowView.Node(rs.getString("node_key"), rs.getString("title"), rs.getString("assignee"), rs.getString("status"), rs.getString("remark")),
                Long.parseLong(id));
        List<WorkflowView.Record> records = jdbcTemplate.query("select * from approval_record where workflow_id=? and deleted=0 order by id",
                (rs, rowNum) -> new WorkflowView.Record(rs.getString("node_id"), rs.getString("applicant"), rs.getString("action"), rs.getString("remark")),
                Long.parseLong(id));
        return new WorkflowView(String.valueOf(wf.get("id")), String.valueOf(wf.get("title")), String.valueOf(wf.get("status")),
                String.valueOf(wf.get("current_node_id")), applicantName, nodes, records);
    }

    @Transactional
    public Map<String, Object> submitCooperationApplication(String applicantUserId, Map<String, Object> body) {
        return submitCooperationApplication(applicantUserId, applicantUserId, null, body);
    }

    @Transactional
    public Map<String, Object> submitCooperationApplication(String applicantUserId, String applicantName, String requestId, Map<String, Object> body) {
        String resourceId = String.valueOf(body.getOrDefault("resourceId", ""));
        if (!StringUtils.hasText(resourceId)) {
            throw new BusinessException(40001, "resourceId不能为空");
        }
        String actualRequestId = StringUtils.hasText(requestId) ? requestId : "auto-" + nextId();
        List<Map<String, Object>> existing = jdbcTemplate.queryForList("""
                select w.id as workflowId,t.id as todoId,w.resource_id as resourceId,w.status
                from workflow w
                left join todo_item t on t.workflow_id=w.id and t.deleted=0
                where w.deleted=0 and w.request_id=?
                limit 1
                """, actualRequestId);
        if (existing != null && !existing.isEmpty()) {
            Map<String, Object> row = existing.get(0);
            return Map.of(
                    "workflowId", String.valueOf(row.get("workflowId")),
                    "todoId", String.valueOf(row.get("todoId")),
                    "resourceId", String.valueOf(row.get("resourceId")),
                    "status", String.valueOf(row.get("status")),
                    "created", false
            );
        }
        ResourceView resource = detail(resourceId);
        if (!"可招商".equals(resource.investmentStatus())) {
            throw new BusinessException(40903, "该资源当前不可提交合作申请");
        }
        Integer duplicateCount = jdbcTemplate.queryForObject("""
                select count(*) from workflow
                where deleted=0 and category=? and resource_id=? and applicant_user_id=? and status=?
                """, Integer.class, "COOPERATION_APPLICATION", Long.parseLong(resourceId), applicantUserId, WorkflowStatus.PENDING.name());
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BusinessException(40900, "该资源已有待审批合作申请，请勿重复提交");
        }

        long workflowId = nextId();
        long todoId = nextId() + 1;
        String title = String.valueOf(body.getOrDefault("title", resource.name() + "合作申请"));
        String assigneeId = String.valueOf(body.getOrDefault("assigneeId", "2"));
        jdbcTemplate.update("""
                insert into workflow(id, village_id, title, category, resource_id, status, current_node_id, applicant, applicant_user_id, request_id, applicant_name, version, created_at)
                values(?,?,?,?,?,?,?,?,?,?,?,?,now())
                """, workflowId, 1, title, "COOPERATION_APPLICATION", Long.parseLong(resourceId), WorkflowStatus.PENDING.name(), "approve", applicantUserId, applicantUserId, actualRequestId, applicantName, 0);
        jdbcTemplate.update("""
                insert into todo_item(id, workflow_id, title, category, status, due_date, assignee, assignee_id)
                values(?,?,?,?,?,date_add(now(), interval 2 day),?,?)
                """, todoId, workflowId, title, "COOPERATION_APPLICATION", WorkflowStatus.PENDING.name(), "staff_demo", assigneeId);
        writeOperationLog(workflowId, Long.parseLong(resourceId), "SUBMIT_APPLICATION", applicantUserId, applicantName, "提交合作申请");
        return Map.of(
                "workflowId", String.valueOf(workflowId),
                "todoId", String.valueOf(todoId),
                "resourceId", resourceId,
                "status", WorkflowStatus.PENDING.name(),
                "created", true
        );
    }

    public List<Map<String, Object>> myApplications(String applicantUserId) {
        return jdbcTemplate.queryForList("""
                select w.id, w.title, w.category, w.resource_id as resourceId, w.status, w.current_node_id as currentNodeId,
                       w.created_at as createdAt, a.remark, a.handled_at as handledAt
                from workflow w
                left join approval_record a on a.workflow_id=w.id and a.deleted=0
                where w.deleted=0 and w.category='COOPERATION_APPLICATION' and w.applicant_user_id=?
                order by w.created_at desc, w.id desc
                """, applicantUserId);
    }

    public Map<String, Object> workbench(String category) {
        List<Map<String, Object>> todos = jdbcTemplate.queryForList("select id,title,category,status,due_date as dueDate,workflow_id as processId from todo_item where deleted=0 order by due_date");
        List<Map<String, Object>> approvals = jdbcTemplate.queryForList("""
                select a.id,a.title,a.applicant,a.amount,a.action,a.status,a.workflow_id as processId,a.handled_at as time,w.category
                from approval_record a
                left join workflow w on w.id=a.workflow_id and w.deleted=0
                where a.deleted=0
                order by a.id desc limit 5
                """);
        return Map.of("todoStats", statsMap(), "approvals", approvals, "filteredTodos", todos, "activeCategory", category == null ? "全部" : category);
    }

    public List<Map<String, Object>> approvalHistory(String approverId, boolean includeAll) {
        if (includeAll) {
            return jdbcTemplate.queryForList("""
                    select a.id,a.title,a.applicant,a.action,a.status,a.remark,a.workflow_id as processId,a.handled_at as time,w.category
                    from approval_record a
                    left join workflow w on w.id=a.workflow_id and w.deleted=0
                    where a.deleted=0
                    order by a.handled_at desc, a.id desc
                    """);
        }
        return jdbcTemplate.queryForList("""
                select a.id,a.title,a.applicant,a.action,a.status,a.remark,a.workflow_id as processId,a.handled_at as time,w.category
                from approval_record a
                left join workflow w on w.id=a.workflow_id and w.deleted=0
                where a.deleted=0 and a.applicant=?
                order by a.handled_at desc, a.id desc
                """, approverId);
    }

    @Transactional
    public Map<String, Object> approve(String id, String action, String approverId, Map<String, Object> body) {
        return approve(id, action, approverId, approverId, body);
    }

    @Transactional
    public Map<String, Object> approve(String id, String action, String approverId, String approverName, Map<String, Object> body) {
        long workflowId = Long.parseLong(id);
        Map<String, Object> workflow = jdbcTemplate.queryForMap("select * from workflow where id=? and deleted=0", workflowId);
        WorkflowStatus currentStatus = WorkflowStatus.from(workflow.get("status"));
        WorkflowStatus nextStatus = normalizeApprovalAction(action);
        if (!currentStatus.canTransitionTo(nextStatus)) {
            throw new BusinessException(40901, "该申请已处理，请勿重复审批");
        }
        int version = workflow.get("version") == null ? 0 : ((Number) workflow.get("version")).intValue();
        String remark = String.valueOf(body.getOrDefault("remark", ""));
        String title = String.valueOf(workflow.get("title"));
        int updated = jdbcTemplate.update("""
                update workflow
                set status=?, current_node_id=?, approver_id=?, approver_name=?, version=version+1, updated_at=now()
                where id=? and deleted=0 and status=? and version=?
                """, nextStatus.name(), "archive", approverId, approverName, workflowId, WorkflowStatus.PENDING.name(), version);
        if (updated == 0) {
            throw new BusinessException(40902, "该申请已被其他工作人员处理，请刷新后重试");
        }
        jdbcTemplate.update("update todo_item set status=? where workflow_id=? and deleted=0 and status=?", nextStatus.name(), workflowId, WorkflowStatus.PENDING.name());
        jdbcTemplate.update("""
                insert into approval_record(id, workflow_id, node_id, title, applicant, action, status, remark, handled_at)
                values(?,?,?,?,?,?,?,?,now())
                """, nextId(), workflowId, body.getOrDefault("nodeId", "approve"), title, approverId, nextStatus.name(), nextStatus.name(), remark);
        Object resourceId = workflow.get("resource_id");
        writeOperationLog(workflowId, resourceId == null ? null : Long.parseLong(String.valueOf(resourceId)), "APPROVE_WORKFLOW", approverId, approverName, remark);
        return Map.of("processId", id, "action", nextStatus.name(), "status", nextStatus.name(), "saved", true);
    }

    public OperationStats stats() {
        int resourceCount = jdbcTemplate.queryForObject("select count(*) from resource where deleted=0", Integer.class);
        int ready = jdbcTemplate.queryForObject("select count(*) from resource where deleted=0 and investment_status='可招商'", Integer.class);
        int workflowCount = jdbcTemplate.queryForObject("select count(*) from workflow where deleted=0", Integer.class);
        int todoCount = jdbcTemplate.queryForObject("select count(*) from todo_item where deleted=0 and status not in ('已完成','APPROVED','REJECTED')", Integer.class);
        int risk = jdbcTemplate.queryForObject("select count(*) from todo_item where deleted=0 and status='已逾期'", Integer.class);
        return new OperationStats(resourceCount, ready, workflowCount, todoCount, risk);
    }

    public ResourceSummary resourceSummary(String id) {
        ResourceView view = detail(id);
        return new ResourceSummary(view.id(), view.name(), view.category(), view.area(), view.annualEstimate(), view.investmentStatus(), view.tags());
    }

    private Map<String, Object> statsMap() {
        OperationStats stats = stats();
        return Map.of("urgent", stats.riskWorkflowCount(), "total", stats.todoCount(), "completedToday", 1);
    }

    private ResourceView toResource(Long id, String name, String category, BigDecimal lat, BigDecimal lng, String address,
                                    BigDecimal area, BigDecimal annualEstimate, String investmentStatus, String intro,
                                    String owner, String contact, String relatedProjects, Integer occupancyRate, Integer expectedRoi) {
        return new ResourceView(String.valueOf(id), name, category, lat, lng, address, area, annualEstimate, investmentStatus,
                loadTags(id), intro, owner, contact, split(relatedProjects), occupancyRate, expectedRoi);
    }

    private Map<String, Object> updateResourceLifecycle(String id, String status, String investmentStatus) {
        int updated = jdbcTemplate.update("""
                update resource
                set status=?, investment_status=?, updated_at=now()
                where id=? and deleted=0
                """, status, investmentStatus, Long.parseLong(id));
        if (updated == 0) {
            throw new BusinessException(40400, "资源不存在");
        }
        redisTemplate.delete("resource:detail:" + id);
        return Map.of("id", id, "status", status, "investmentStatus", investmentStatus, "updated", true);
    }

    private List<String> loadTags(Long resourceId) {
        return jdbcTemplate.queryForList("""
                select t.name from resource_tag t
                join resource_tag_rel r on r.tag_id=t.id
                where r.resource_id=? and t.deleted=0 order by t.sort_no
                """, String.class, resourceId);
    }

    private List<String> split(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(StringUtils::hasText).toList();
    }

    private WorkflowStatus normalizeApprovalAction(String action) {
        if ("pass".equalsIgnoreCase(action) || "approve".equalsIgnoreCase(action) || "APPROVED".equalsIgnoreCase(action)) {
            return WorkflowStatus.APPROVED;
        }
        if ("reject".equalsIgnoreCase(action) || "REJECTED".equalsIgnoreCase(action)) {
            return WorkflowStatus.REJECTED;
        }
        throw new BusinessException(40001, "审批动作不合法");
    }

    private void writeOperationLog(Long workflowId, Long resourceId, String action, String operatorId, String operatorName, String remark) {
        jdbcTemplate.update("""
                insert into operation_log(id, workflow_id, resource_id, action, operator_id, operator_name, remark, created_at)
                values(?,?,?,?,?,?,?,now())
                """, nextId(), workflowId, resourceId, action, operatorId, operatorName, remark);
    }

    private long nextId() {
        return System.currentTimeMillis() % 1000000000;
    }
}
