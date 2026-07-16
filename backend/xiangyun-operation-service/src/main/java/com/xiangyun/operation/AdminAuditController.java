package com.xiangyun.operation;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.BusinessException;
import com.xiangyun.common.SecurityHeaders;
import com.xiangyun.common.dto.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
public class AdminAuditController {

    private final AdminAuditService auditService;

    public AdminAuditController(AdminAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Map<String, Object>>> page(
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        requireAdmin(role);
        return ApiResponse.success(auditService.page(
                keyword, module, result, startTime, endTime, page, pageSize));
    }

    @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> export(
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        requireAdmin(role);
        List<Map<String, Object>> rows = auditService.export(keyword, module, result, startTime, endTime);
        byte[] content = ("\uFEFF" + csv(rows)).getBytes(StandardCharsets.UTF_8);
        String filename = "乡耘OS_管理员审计_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .contentLength(content.length)
                .body(content);
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(40301, "仅管理员可查看审计日志");
        }
    }

    private String csv(List<Map<String, Object>> rows) {
        StringBuilder csv = new StringBuilder("时间,结果,HTTP状态,模块,操作,操作人,角色,村域,对象类型,对象编号,请求方法,请求路径,客户端IP,Trace ID,处理信息\r\n");
        for (Map<String, Object> row : rows) {
            appendRow(csv, row, "createdAt", "result", "httpStatus", "module", "action", "actorName",
                    "actorRole", "villageId", "targetType", "targetId", "requestMethod", "requestPath",
                    "clientIp", "traceId", "detail");
        }
        return csv.toString();
    }

    private void appendRow(StringBuilder csv, Map<String, Object> row, String... fields) {
        for (int index = 0; index < fields.length; index++) {
            if (index > 0) csv.append(',');
            csv.append(csvCell(row.get(fields[index])));
        }
        csv.append("\r\n");
    }

    private String csvCell(Object value) {
        String text = value == null ? "" : String.valueOf(value).replace("\r", " ").replace("\n", " ");
        if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        return '"' + text.replace("\"", "\"\"") + '"';
    }
}
