package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminAuditControllerTest {

    @Test
    void rejectsNonAdminEvenBehindInternalServiceBoundary() {
        AdminAuditController controller = new AdminAuditController(mock(AdminAuditService.class));

        assertThatThrownBy(() -> controller.page("STAFF", null, null, null, null, null, 1, 20))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅管理员");
    }

    @Test
    void exportsExcelFriendlyCsvAndNeutralizesFormulaCells() {
        AdminAuditService service = mock(AdminAuditService.class);
        when(service.export(any(), any(), any(), isNull(), isNull())).thenReturn(List.of(Map.of(
                "createdAt", "2026-07-16 09:00:00",
                "result", "SUCCESS",
                "httpStatus", 200,
                "module", "SECURITY",
                "action", "LOGIN_SUCCESS",
                "actorName", "=danger",
                "traceId", "trace-1"
        )));
        AdminAuditController controller = new AdminAuditController(service);

        byte[] body = controller.export("ADMIN", null, null, null, null, null).getBody();
        String csv = new String(body, StandardCharsets.UTF_8);

        assertThat(csv).startsWith("\uFEFF时间,结果");
        assertThat(csv).contains("\"'=danger\"");
        assertThat(csv).contains("trace-1");
    }
}
