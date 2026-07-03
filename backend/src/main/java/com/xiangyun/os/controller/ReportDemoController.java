package com.xiangyun.os.controller;

import com.xiangyun.os.common.ApiResponse;
import com.xiangyun.os.service.ReportService;
import com.xiangyun.os.vo.ReportDashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "Report Demo Extension")
@RestController
@RequiredArgsConstructor
public class ReportDemoController {

    private final ReportService reportService;

    @Operation(summary = "Get report summary")
    @GetMapping("/reports/summary")
    public ApiResponse<List<ReportDashboardVO.ReportSummary>> summary(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(reportService.getReportDashboard(period).getSummary());
    }

    @Operation(summary = "Get visitor trends")
    @GetMapping("/reports/visitor-trends")
    public ApiResponse<List<?>> visitorTrends(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(reportService.getReportDashboard(period).getFlowPoints());
    }

    @Operation(summary = "Get revenue bars")
    @GetMapping("/reports/revenue-bars")
    public ApiResponse<ReportDashboardVO.RevenueBar> revenueBars(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(reportService.getReportDashboard(period).getRevenueBar());
    }

    @Operation(summary = "Get revenue ratio")
    @GetMapping("/reports/revenue-ratio")
    public ApiResponse<ReportDashboardVO.RevenueRatio> revenueRatio(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(reportService.getReportDashboard(period).getRatioRing());
    }

    @Operation(summary = "Get auto summary")
    @GetMapping("/reports/auto-summary")
    public ApiResponse<Map<String, Object>> autoSummary(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(Map.of("summary", reportService.getReportDashboard(period).getAutoSummary()));
    }

    @Operation(summary = "Get report AI suggestions")
    @GetMapping("/reports/suggestions")
    public ApiResponse<List<?>> suggestions(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(reportService.getReportDashboard(period).getAiTips());
    }

    @Operation(summary = "Get investment match view")
    @GetMapping("/reports/investment-match-view")
    public ApiResponse<Map<String, Object>> investmentMatchView(@RequestParam(defaultValue = "1") String resourceId) {
        return ApiResponse.success(Map.of(
                "resourceId", resourceId,
                "matches", List.of(
                        Map.of("investor", "Agri-tourism Partner A", "score", 92, "reason", "Strong fit with weekend tourism"),
                        Map.of("investor", "Local Product Channel B", "score", 86, "reason", "Can package local products")
                ),
                "aiSummary", Map.of("title", "Prioritize cultural tourism bundle", "priority", "P1")
        ));
    }

    @Operation(summary = "Get forecast view")
    @GetMapping("/reports/forecast")
    public ApiResponse<Map<String, Object>> forecast() {
        return ApiResponse.success(Map.of(
                "forecastData", List.of(
                        Map.of("date", "07-01", "predict", 360, "upper", 410, "lower", 315),
                        Map.of("date", "07-02", "predict", 390, "upper", 448, "lower", 340),
                        Map.of("date", "07-03", "predict", 430, "upper", 486, "lower", 380)
                ),
                "band", Map.of("confidence", "85%", "model", "moving-average-demo"),
                "strategies", List.of("Increase guide staffing", "Prepare parking diversion", "Promote local product bundle")
        ));
    }

    @Operation(summary = "Export report demo")
    @PostMapping("/reports/export")
    public ApiResponse<Map<String, Object>> exportReport(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("taskId", "export-demo-001", "format", body.getOrDefault("format", "pdf"), "status", "created"));
    }

    @Operation(summary = "Create report snapshot demo")
    @PostMapping("/reports/snapshots")
    public ApiResponse<Map<String, Object>> createSnapshot(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("snapshotId", "snapshot-demo-new", "saved", true, "payload", body));
    }

    @Operation(summary = "List report periods")
    @GetMapping("/reports/periods")
    public ApiResponse<List<Map<String, Object>>> periods() {
        return ApiResponse.success(List.of(
                Map.of("key", "7d", "label", "Last 7 days"),
                Map.of("key", "30d", "label", "Last 30 days")
        ));
    }

    @Operation(summary = "Get KPI target progress")
    @GetMapping("/reports/kpi-progress")
    public ApiResponse<List<Map<String, Object>>> kpiProgress() {
        return ApiResponse.success(List.of(
                Map.of("name", "visitor", "actual", 2680, "target", 3000, "rate", new BigDecimal("89.3")),
                Map.of("name", "revenue", "actual", 18.6, "target", 20, "rate", new BigDecimal("93.0"))
        ));
    }
}
