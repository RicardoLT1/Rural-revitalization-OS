package com.xiangyun.operation;

import com.xiangyun.common.dto.PageResponse;
import com.xiangyun.common.dto.AdminAuditRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminAuditService {

    private static final String AUDIT_SELECT = """
            select id,trace_id as traceId,actor_id as actorId,actor_name as actorName,
                   actor_role as actorRole,village_id as villageId,module,action,
                   target_type as targetType,target_id as targetId,request_method as requestMethod,
                   request_path as requestPath,client_ip as clientIp,user_agent as userAgent,
                   result,http_status as httpStatus,detail,before_data as beforeData,
                   after_data as afterData,created_at as createdAt
            from admin_audit_log
            """;

    private final JdbcTemplate jdbcTemplate;

    public AdminAuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AdminAuditEvent event) {
        jdbcTemplate.update("""
                insert into admin_audit_log(
                  trace_id,actor_id,actor_name,actor_role,village_id,module,action,target_type,target_id,
                  request_method,request_path,client_ip,user_agent,result,http_status,detail,before_data,after_data
                ) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                limit(event.traceId(), 64), limit(event.actorId(), 64), limit(event.actorName(), 128),
                limit(event.actorRole(), 32), limit(event.villageId(), 64), limit(event.module(), 64),
                limit(event.action(), 96), limit(event.targetType(), 64), limit(event.targetId(), 128),
                limit(event.requestMethod(), 16), limit(event.requestPath(), 255), limit(event.clientIp(), 64),
                limit(event.userAgent(), 512), limit(event.result(), 16), event.httpStatus(), limit(event.detail(), 512),
                event.beforeData(), event.afterData());
    }

    public void record(AdminAuditRequest request) {
        record(new AdminAuditEvent(
                request.traceId(), request.actorId(), request.actorName(), request.actorRole(), request.villageId(),
                request.module(), request.action(), request.targetType(), request.targetId(), request.requestMethod(),
                request.requestPath(), request.clientIp(), request.userAgent(), request.result(),
                request.httpStatus() == null ? 200 : request.httpStatus(), request.detail(), request.beforeData(), request.afterData()));
    }

    public PageResponse<Map<String, Object>> page(String keyword,
                                                  String module,
                                                  String result,
                                                  LocalDateTime startTime,
                                                  LocalDateTime endTime,
                                                  Integer page,
                                                  Integer pageSize) {
        int actualPage = PageResponse.normalizePage(page);
        int actualPageSize = PageResponse.normalizePageSize(pageSize);
        AuditQuery query = auditQuery(keyword, module, result, startTime, endTime);
        String where = query.where();
        List<Object> args = query.args();
        Integer totalValue = jdbcTemplate.queryForObject(
                "select count(*) from admin_audit_log" + where,
                Integer.class,
                args.toArray());
        int total = totalValue == null ? 0 : totalValue;
        List<Object> dataArgs = new ArrayList<>(args);
        dataArgs.add(actualPageSize);
        dataArgs.add((actualPage - 1) * actualPageSize);
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
                AUDIT_SELECT + where + " order by created_at desc,id desc limit ? offset ?", dataArgs.toArray());
        return PageResponse.of(items, actualPage, actualPageSize, total);
    }

    public List<Map<String, Object>> export(String keyword,
                                            String module,
                                            String result,
                                            LocalDateTime startTime,
                                            LocalDateTime endTime) {
        AuditQuery query = auditQuery(keyword, module, result, startTime, endTime);
        List<Object> args = new ArrayList<>(query.args());
        args.add(5000);
        return jdbcTemplate.queryForList(
                AUDIT_SELECT + query.where() + " order by created_at desc,id desc limit ?", args.toArray());
    }

    private AuditQuery auditQuery(String keyword,
                                  String module,
                                  String result,
                                  LocalDateTime startTime,
                                  LocalDateTime endTime) {
        StringBuilder where = new StringBuilder(" where 1=1");
        List<Object> args = new ArrayList<>();
        if (StringUtils.hasText(module) && !"ALL".equalsIgnoreCase(module)) {
            where.append(" and module=?");
            args.add(module);
        }
        if (StringUtils.hasText(result) && !"ALL".equalsIgnoreCase(result)) {
            where.append(" and result=?");
            args.add(result);
        }
        if (StringUtils.hasText(keyword)) {
            where.append(" and (actor_name like ? or action like ? or target_id like ? or trace_id like ?)");
            String term = "%" + keyword.trim() + "%";
            args.add(term);
            args.add(term);
            args.add(term);
            args.add(term);
        }
        if (startTime != null) {
            where.append(" and created_at>=?");
            args.add(startTime);
        }
        if (endTime != null) {
            where.append(" and created_at<=?");
            args.add(endTime);
        }
        return new AuditQuery(where.toString(), args);
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record AuditQuery(String where, List<Object> args) {
    }
}
