package com.xiangyun.analysis;

import com.xiangyun.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalysisControllerTest {

    private final AnalysisService analysisService = mock(AnalysisService.class);
    private final AnalysisController controller = new AnalysisController(analysisService);

    @Test
    void dashboardWritesStaleHeaderAndReturnsText() {
        when(analysisService.dashboard("1", 7)).thenReturn(new DashboardResult(
                Map.of("stats", List.of(Map.of("title", "资源总数", "unit", "个"))),
                "STALE",
                true
        ));
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.dashboard("1", 7, response);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeader("X-Cache-Status")).isEqualTo("STALE");
        assertThat(response.getHeader("X-Data-Stale")).isEqualTo("true");
        assertThat(result.getBody().data().get("cacheStatus")).isEqualTo("STALE");
        assertThat(result.getBody().data().get("stale")).isEqualTo(true);
        List<?> stats = (List<?>) result.getBody().data().get("stats");
        Map<?, ?> first = (Map<?, ?>) stats.get(0);
        assertThat(first.get("title")).isEqualTo("资源总数");
        assertThat(first.get("unit")).isEqualTo("个");
    }

    @Test
    void dashboardReturnsFriendly503WhenUnavailable() {
        when(analysisService.dashboard("1", null)).thenThrow(new DashboardUnavailableException("internal"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<ApiResponse<Map<String, Object>>> result = controller.dashboard("1", null, response);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(result.getBody().message()).isEqualTo("统计服务暂时不可用，请稍后重试");
    }
}
