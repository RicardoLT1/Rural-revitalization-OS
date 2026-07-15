package com.xiangyun.operation;

import com.xiangyun.common.dto.AdminAuditRequest;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InternalAuditControllerTest {

    @Test
    void acceptsTrustedCrossServiceAuditEvent() {
        AdminAuditService service = mock(AdminAuditService.class);
        InternalAuditController controller = new InternalAuditController(service);
        AdminAuditRequest request = new AdminAuditRequest(
                "trace-1", "3", "admin", "ADMIN", "1", "USER", "UPDATE_USER",
                "USER", "2", "PUT", "/api/users/2", "127.0.0.1", "test",
                "SUCCESS", 200, "done", "{}", "{}");

        controller.record(request);

        verify(service).record(request);
    }
}
