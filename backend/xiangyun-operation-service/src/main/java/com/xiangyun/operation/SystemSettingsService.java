package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class SystemSettingsService {

    private static final String VERSION = "v1.3-admin-pro";
    private final JdbcTemplate jdbcTemplate;

    public SystemSettingsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SystemSettingsView get(String villageId) {
        long id = villageId(villageId);
        List<SystemSettingsView> rows = jdbcTemplate.query("""
                select village_id,platform_name,village_name,map_center_lat,map_center_lng,
                       approval_timeout_hours,weekly_report_day,workflow_notification_enabled,
                       risk_notification_enabled,contact_phone,updated_by,updated_at
                from system_setting where village_id=?
                """, this::map, id);
        if (!rows.isEmpty()) return rows.get(0);
        String villageName = jdbcTemplate.queryForObject(
                "select name from village where id=? and deleted=0", String.class, id);
        if (!StringUtils.hasText(villageName)) throw new BusinessException(40400, "村域不存在");
        jdbcTemplate.update("""
                insert into system_setting(
                  village_id,platform_name,village_name,map_center_lat,map_center_lng,
                  approval_timeout_hours,weekly_report_day,workflow_notification_enabled,
                  risk_notification_enabled,contact_phone,updated_by
                ) values(?,?,?,?,?,?,?,?,?,?,?)
                """, id, "乡耘 OS", villageName, new BigDecimal("30.640522"), new BigDecimal("119.681337"),
                24, 1, true, true, "", "system");
        return get(villageId);
    }

    @Transactional
    public SystemSettingsView update(String villageId, String operator, Map<String, Object> body) {
        SystemSettingsView current = get(villageId);
        String platformName = text(body, "platformName", current.platformName(), 2, 64, "平台名称");
        String villageName = text(body, "villageName", current.villageName(), 2, 128, "村域名称");
        BigDecimal lat = decimal(body, "mapCenterLat", current.mapCenterLat(), -90, 90, "地图中心纬度");
        BigDecimal lng = decimal(body, "mapCenterLng", current.mapCenterLng(), -180, 180, "地图中心经度");
        int timeout = integer(body, "approvalTimeoutHours", current.approvalTimeoutHours(), 1, 168, "审批超时阈值");
        int reportDay = integer(body, "weeklyReportDay", current.weeklyReportDay(), 1, 7, "周报生成日");
        boolean workflowNotice = bool(body, "workflowNotificationEnabled", current.workflowNotificationEnabled());
        boolean riskNotice = bool(body, "riskNotificationEnabled", current.riskNotificationEnabled());
        String contactPhone = text(body, "contactPhone", current.contactPhone(), 0, 32, "联系电话");
        long id = villageId(villageId);
        jdbcTemplate.update("""
                update system_setting set platform_name=?,village_name=?,map_center_lat=?,map_center_lng=?,
                  approval_timeout_hours=?,weekly_report_day=?,workflow_notification_enabled=?,
                  risk_notification_enabled=?,contact_phone=?,updated_by=?,updated_at=current_timestamp
                where village_id=?
                """, platformName, villageName, lat, lng, timeout, reportDay, workflowNotice, riskNotice,
                contactPhone, operator, id);
        jdbcTemplate.update("update village set name=? where id=? and deleted=0", villageName, id);
        return get(villageId);
    }

    private SystemSettingsView map(ResultSet rs, int rowNum) throws SQLException {
        return new SystemSettingsView(
                String.valueOf(rs.getLong("village_id")), rs.getString("platform_name"),
                rs.getString("village_name"), rs.getBigDecimal("map_center_lat"),
                rs.getBigDecimal("map_center_lng"), rs.getInt("approval_timeout_hours"),
                rs.getInt("weekly_report_day"), rs.getBoolean("workflow_notification_enabled"),
                rs.getBoolean("risk_notification_enabled"), rs.getString("contact_phone"),
                rs.getString("updated_by"), rs.getTimestamp("updated_at").toLocalDateTime(), VERSION);
    }

    private long villageId(String value) {
        try { return Long.parseLong(value); }
        catch (Exception ex) { throw new BusinessException(40010, "村域编号不合法"); }
    }

    private String text(Map<String, Object> body, String key, String fallback, int min, int max, String label) {
        String value = body.containsKey(key) ? String.valueOf(body.getOrDefault(key, "")).trim() : fallback;
        if (value.length() < min || value.length() > max) {
            throw new BusinessException(40011, label + "长度应在 " + min + " 到 " + max + " 个字符之间");
        }
        return value;
    }

    private BigDecimal decimal(Map<String, Object> body, String key, BigDecimal fallback,
                               double min, double max, String label) {
        try {
            BigDecimal value = body.containsKey(key) ? new BigDecimal(String.valueOf(body.get(key))) : fallback;
            if (value.doubleValue() < min || value.doubleValue() > max) throw new NumberFormatException();
            return value;
        } catch (Exception ex) {
            throw new BusinessException(40012, label + "不合法");
        }
    }

    private int integer(Map<String, Object> body, String key, int fallback, int min, int max, String label) {
        try {
            int value = body.containsKey(key) ? Integer.parseInt(String.valueOf(body.get(key))) : fallback;
            if (value < min || value > max) throw new NumberFormatException();
            return value;
        } catch (Exception ex) {
            throw new BusinessException(40013, label + "不合法");
        }
    }

    private boolean bool(Map<String, Object> body, String key, boolean fallback) {
        return body.containsKey(key) ? Boolean.parseBoolean(String.valueOf(body.get(key))) : fallback;
    }
}
