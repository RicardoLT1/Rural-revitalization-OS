package com.xiangyun.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.SecurityHeaders;
import com.xiangyun.common.dto.AdminAuditRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuthAuditPublisherTest {

    @Test
    void publishesSanitizedUserChangeWithActorContext() {
        AuditClient client = mock(AuditClient.class);
        AuthAuditPublisher publisher = new AuthAuditPublisher(client, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/users/2");
        request.addHeader(SecurityHeaders.TRACE_ID, "trace-user-1");
        request.addHeader(SecurityHeaders.USER_ID, "3");
        request.addHeader(SecurityHeaders.USERNAME, "admin");
        request.addHeader(SecurityHeaders.ROLE, "ADMIN");
        request.addHeader(SecurityHeaders.VILLAGE_ID, "1");

        publisher.record(request, "UPDATE_USER", "USER", "2", "SUCCESS", 200, "done",
                Map.of("role", "USER"), Map.of("role", "STAFF"));

        ArgumentCaptor<AdminAuditRequest> captor = ArgumentCaptor.forClass(AdminAuditRequest.class);
        verify(client).record(eq("trace-user-1"), captor.capture());
        assertThat(captor.getValue().actorName()).isEqualTo("admin");
        assertThat(captor.getValue().beforeData()).contains("USER");
        assertThat(captor.getValue().afterData()).contains("STAFF");
    }
}
