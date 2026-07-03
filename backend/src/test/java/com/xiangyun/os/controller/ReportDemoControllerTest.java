package com.xiangyun.os.controller;

import com.xiangyun.os.common.ApiResponse;
import com.xiangyun.os.service.ReportService;
import com.xiangyun.os.vo.CommonVO;
import com.xiangyun.os.vo.ReportDashboardVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportDemoControllerTest {

    @Test
    void summaryUsesReportServiceData() {
        ReportDemoController controller = new ReportDemoController(new StubReportService());

        ApiResponse<List<ReportDashboardVO.ReportSummary>> response = controller.summary("7d");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getId()).isEqualTo("visitor");
    }

    @Test
    void forecastReturnsStrategyList() {
        ReportDemoController controller = new ReportDemoController(new StubReportService());

        ApiResponse<Map<String, Object>> response = controller.forecast();

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).containsKeys("forecastData", "band", "strategies");
    }

    private static class StubReportService implements ReportService {
        @Override
        public ReportDashboardVO getReportDashboard(String period) {
            return new ReportDashboardVO(
                    List.of(new ReportDashboardVO.ReportSummary("visitor", "Visitors", "100", "+5%")),
                    List.of(new CommonVO.OptionItem("7d", "Last 7 days")),
                    "7d",
                    List.of(new CommonVO.TrendPoint("06-26", 100)),
                    new ReportDashboardVO.RevenueBar(List.of("06-26"), List.of(new CommonVO.ChartSeries("Revenue", List.of(BigDecimal.TEN)))),
                    new ReportDashboardVO.RevenueRatio(List.of("culture"), List.of(BigDecimal.valueOf(100)), List.of("#2F7D32")),
                    "Demo summary",
                    List.of(new CommonVO.AiSuggestion("ai-1", "Tip", "Content", "P1", "Action", "match", "demo"))
            );
        }
    }
}
