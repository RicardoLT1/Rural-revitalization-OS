package com.xiangyun.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.SecurityHeaders;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AdminAuditFilterTest {

    @Test
    void recordsAuthenticatedResourceWrite() throws Exception {
        AdminAuditService auditService = mock(AdminAuditService.class);
        AdminAuditFilter filter = new AdminAuditFilter(auditService, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/resources/101/publish");
        request.addHeader(SecurityHeaders.TRACE_ID, "trace-1");
        request.addHeader(SecurityHeaders.USER_ID, "3");
        request.addHeader(SecurityHeaders.USERNAME, "admin");
        request.addHeader(SecurityHeaders.ROLE, "ADMIN");
        request.addHeader(SecurityHeaders.VILLAGE_ID, "1");
        AdminAuditContext.before(request, java.util.Map.of("investmentStatus", "洽谈中"));
        AdminAuditContext.after(request, java.util.Map.of("investmentStatus", "可招商"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (nextRequest, nextResponse) -> ((MockHttpServletResponse) nextResponse).setStatus(200);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<AdminAuditEvent> captor = ArgumentCaptor.forClass(AdminAuditEvent.class);
        verify(auditService).record(captor.capture());
        assertThat(captor.getValue().action()).isEqualTo("PUBLISH_RESOURCE");
        assertThat(captor.getValue().targetId()).isEqualTo("101");
        assertThat(captor.getValue().result()).isEqualTo("SUCCESS");
        assertThat(captor.getValue().traceId()).isEqualTo("trace-1");
        assertThat(captor.getValue().beforeData()).contains("洽谈中");
        assertThat(captor.getValue().afterData()).contains("可招商");
    }

    @Test
    void ignoresReadOnlyRequests() throws Exception {
        AdminAuditService auditService = mock(AdminAuditService.class);
        AdminAuditFilter filter = new AdminAuditFilter(auditService, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/resources");

        filter.doFilter(request, new MockHttpServletResponse(), (nextRequest, nextResponse) -> { });

        verify(auditService, never()).record(org.mockito.ArgumentMatchers.any(AdminAuditEvent.class));
    }

    @Test
    void identifiesResourceMaterialUploadAsDedicatedAuditAction() throws Exception {
        AdminAuditService auditService = mock(AdminAuditService.class);
        AdminAuditFilter filter = new AdminAuditFilter(auditService, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/resources/101/materials");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (nextRequest, nextResponse) -> {
            AdminAuditContext.targetId((MockHttpServletRequest) nextRequest, "901");
            ((MockHttpServletResponse) nextResponse).setStatus(200);
        });

        ArgumentCaptor<AdminAuditEvent> captor = ArgumentCaptor.forClass(AdminAuditEvent.class);
        verify(auditService).record(captor.capture());
        assertThat(captor.getValue().action()).isEqualTo("UPLOAD_RESOURCE_MATERIAL");
        assertThat(captor.getValue().targetType()).isEqualTo("RESOURCE_MATERIAL");
        assertThat(captor.getValue().targetId()).isEqualTo("901");
    }
}
