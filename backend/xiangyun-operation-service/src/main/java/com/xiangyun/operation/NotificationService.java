package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private static final DateTimeFormatter NOTICE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final JdbcTemplate jdbcTemplate;
    private final SystemSettingsService settingsService;

    public NotificationService(JdbcTemplate jdbcTemplate, SystemSettingsService settingsService) {
        this.jdbcTemplate = jdbcTemplate;
        this.settingsService = settingsService;
    }

    @Transactional
    public NotificationCenterView center(String villageId, String userId, String role, boolean unreadOnly, int limit) {
        requireStaff(role);
        long village = parseVillage(villageId);
        syncOperationalNotifications(villageId);
        int actualLimit = Math.max(1, Math.min(limit, 50));
        String unreadClause = unreadOnly ? " and nr.notification_id is null" : "";
        List<NotificationItemView> items = jdbcTemplate.query("""
                select n.id,n.type,n.title,n.content,n.target_path,n.created_at,nr.read_at
                from admin_notification n
                left join admin_notification_read nr on nr.notification_id=n.id and nr.user_id=?
                where n.village_id=? and n.active=1
                  and (n.target_role='STAFF_ADMIN' or n.target_role=? or n.target_role='ALL')
                """ + unreadClause + " order by n.updated_at desc,n.id desc limit ?",
                this::map, userId, village, role, actualLimit);
        Integer total = jdbcTemplate.queryForObject("""
                select count(*) from admin_notification n
                where n.village_id=? and n.active=1
                  and (n.target_role='STAFF_ADMIN' or n.target_role=? or n.target_role='ALL')
                """, Integer.class, village, role);
        Integer unread = jdbcTemplate.queryForObject("""
                select count(*) from admin_notification n
                left join admin_notification_read nr on nr.notification_id=n.id and nr.user_id=?
                where n.village_id=? and n.active=1 and nr.notification_id is null
                  and (n.target_role='STAFF_ADMIN' or n.target_role=? or n.target_role='ALL')
                """, Integer.class, userId, village, role);
        return new NotificationCenterView(items, unread == null ? 0 : unread, total == null ? 0 : total);
    }

    @Transactional
    public Map<String, Object> markRead(String id, String villageId, String userId, String role) {
        requireStaff(role);
        long notificationId = parseId(id);
        Integer visible = jdbcTemplate.queryForObject("""
                select count(*) from admin_notification
                where id=? and village_id=? and active=1
                  and (target_role='STAFF_ADMIN' or target_role=? or target_role='ALL')
                """, Integer.class, notificationId, parseVillage(villageId), role);
        if (visible == null || visible == 0) throw new BusinessException(40400, "通知不存在或已失效");
        jdbcTemplate.update("insert ignore into admin_notification_read(notification_id,user_id) values(?,?)",
                notificationId, userId);
        return Map.of("id", id, "read", true);
    }

    @Transactional
    public Map<String, Object> markAllRead(String villageId, String userId, String role) {
        requireStaff(role);
        int changed = jdbcTemplate.update("""
                insert ignore into admin_notification_read(notification_id,user_id)
                select id,? from admin_notification
                where village_id=? and active=1
                  and (target_role='STAFF_ADMIN' or target_role=? or target_role='ALL')
                """, userId, parseVillage(villageId), role);
        return Map.of("read", true, "changed", changed);
    }

    private void syncOperationalNotifications(String villageId) {
        long village = parseVillage(villageId);
        SystemSettingsView settings = settingsService.get(villageId);
        jdbcTemplate.update("""
                update admin_notification set active=0,updated_at=current_timestamp
                where village_id=? and type in ('WORKFLOW_PENDING','WORKFLOW_OVERDUE')
                """, village);
        if (settings.workflowNotificationEnabled()) {
            List<PendingWorkflow> pending = jdbcTemplate.query("""
                    select w.id,w.title,
                           min(coalesce(t.due_date,timestampadd(hour,?,w.created_at))) as due_date
                    from workflow w left join todo_item t on t.workflow_id=w.id and t.deleted=0
                    where w.village_id=? and w.deleted=0 and w.status='PENDING'
                    group by w.id,w.title order by w.updated_at desc,w.id desc
                    """, (rs, rowNum) -> new PendingWorkflow(
                    rs.getLong("id"), rs.getString("title"),
                    rs.getTimestamp("due_date") == null ? null : rs.getTimestamp("due_date").toLocalDateTime()),
                    settings.approvalTimeoutHours(), village);
            for (PendingWorkflow item : pending) {
                String due = item.dueDate() == null ? "等待工作人员处理"
                        : "处理时限 " + NOTICE_TIME.format(item.dueDate());
                upsert(village, "workflow-pending:" + item.id(), "WORKFLOW_PENDING",
                        item.title(), due, "/approvals?workflow=" + item.id());
            }
        }
        if (settings.riskNotificationEnabled()) {
            Integer overdue = jdbcTemplate.queryForObject("""
                    select count(*) from todo_item t join workflow w on w.id=t.workflow_id
                    where w.village_id=? and w.deleted=0 and w.status='PENDING'
                      and t.deleted=0 and t.status='PENDING'
                      and (t.due_date<now() or timestampadd(hour,?,w.created_at)<now())
                    """, Integer.class, village, settings.approvalTimeoutHours());
            if (overdue != null && overdue > 0) {
                upsert(village, "workflow-overdue", "WORKFLOW_OVERDUE",
                        "审批流程已超时", "当前有 " + overdue + " 条待办超过截止时间，请尽快核查。",
                        "/approvals?status=PENDING");
            }
        }
    }

    private void upsert(long village, String businessKey, String type,
                        String title, String content, String targetPath) {
        jdbcTemplate.update("""
                insert into admin_notification(
                  village_id,business_key,type,title,content,target_path,target_role,active
                ) values(?,?,?,?,?,?,?,1)
                on duplicate key update type=values(type),title=values(title),content=values(content),
                  target_path=values(target_path),target_role=values(target_role),active=1,
                  updated_at=current_timestamp
                """, village, businessKey, type, title, content, targetPath, "STAFF_ADMIN");
    }

    private NotificationItemView map(ResultSet rs, int rowNum) throws SQLException {
        return new NotificationItemView(String.valueOf(rs.getLong("id")), rs.getString("type"),
                rs.getString("title"), rs.getString("content"), rs.getString("target_path"),
                rs.getTimestamp("read_at") != null, rs.getTimestamp("created_at").toLocalDateTime());
    }

    private void requireStaff(String role) {
        if (!"STAFF".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(40300, "无权访问管理通知");
        }
    }

    private long parseVillage(String value) {
        try { return Long.parseLong(value); }
        catch (Exception ex) { throw new BusinessException(40010, "村域编号不合法"); }
    }

    private long parseId(String value) {
        try { return Long.parseLong(value); }
        catch (Exception ex) { throw new BusinessException(40014, "通知编号不合法"); }
    }

    private record PendingWorkflow(long id, String title, LocalDateTime dueDate) {}
}
