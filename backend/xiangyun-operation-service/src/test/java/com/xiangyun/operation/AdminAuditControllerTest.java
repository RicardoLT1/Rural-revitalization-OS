package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AdminAuditControllerTest {

    @Test
    void rejectsNonAdminEvenBehindInternalServiceBoundary() {
        AdminAuditController controller = new AdminAuditController(mock(AdminAuditService.class));

        assertThatThrownBy(() -> controller.page("STAFF", null, null, null, 1, 20))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅管理员");
    }
}
