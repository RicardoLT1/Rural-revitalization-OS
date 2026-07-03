package com.xiangyun.analysis;

import com.xiangyun.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard(@RequestParam(defaultValue = "1") String villageId,
                                                                      @RequestParam(required = false) Integer days,
                                                                      HttpServletResponse response) {
        try {
            DashboardResult result = analysisService.dashboard(villageId, days);
            response.setHeader("X-Cache-Status", result.cacheStatus());
            if (result.stale()) {
                response.setHeader("X-Data-Stale", "true");
            }
            return ResponseEntity.ok(ApiResponse.success(ResponseTextSanitizer.cleanMap(result.data())));
        } catch (DashboardUnavailableException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.fail(50300, "统计服务暂时不可用，请稍后重试"));
        }
    }

    @PostMapping("/dashboard/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshDashboard(@RequestParam(defaultValue = "1") String villageId,
                                                                            @RequestParam(required = false) Integer days,
                                                                            HttpServletResponse response) {
        try {
            DashboardResult result = analysisService.refreshDashboard(villageId, days);
            response.setHeader("X-Cache-Status", result.cacheStatus());
            if (result.stale()) {
                response.setHeader("X-Data-Stale", "true");
            }
            return ResponseEntity.ok(ApiResponse.success(ResponseTextSanitizer.cleanMap(result.data())));
        } catch (DashboardUnavailableException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.fail(50300, "统计服务暂时不可用，请稍后重试"));
        }
    }

    @GetMapping("/reports/dashboard")
    public ApiResponse<Map<String, Object>> reportDashboard(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(ResponseTextSanitizer.cleanMap(analysisService.reportDashboard(period)));
    }

    @GetMapping("/reports/summary")
    public ApiResponse<Object> reportSummary(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(ResponseTextSanitizer.clean(analysisService.reportDashboard(period).get("summary")));
    }

    @GetMapping("/reports/visitor-trends")
    public ApiResponse<Object> visitorTrends(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(analysisService.reportDashboard(period).get("flowPoints"));
    }

    @GetMapping("/reports/revenue-bars")
    public ApiResponse<Object> revenueBars(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(ResponseTextSanitizer.clean(analysisService.reportDashboard(period).get("revenueBar")));
    }

    @GetMapping("/reports/revenue-ratio")
    public ApiResponse<Object> revenueRatio(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(ResponseTextSanitizer.clean(analysisService.reportDashboard(period).get("ratioRing")));
    }

    @GetMapping("/reports/auto-summary")
    public ApiResponse<Object> autoSummary(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(ResponseTextSanitizer.clean(analysisService.reportDashboard(period).get("autoSummary")));
    }

    @GetMapping("/reports/suggestions")
    public ApiResponse<Object> suggestions(@RequestParam(defaultValue = "7d") String period) {
        return ApiResponse.success(ResponseTextSanitizer.clean(analysisService.reportDashboard(period).get("aiTips")));
    }

    @GetMapping("/reports/forecast")
    public ApiResponse<Object> reportForecast() {
        return ApiResponse.success(Map.of("forecastData", analysisService.forecast(), "strategies", java.util.List.of("提前准备周末接待", "加强停车分流")));
    }

    @GetMapping("/forecasts")
    public ApiResponse<Object> forecasts() {
        return ApiResponse.success(analysisService.forecast());
    }

    @PostMapping("/forecasts")
    public ApiResponse<Map<String, Object>> createForecast(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("created", true, "payload", body));
    }

    @PutMapping("/forecasts/{id}")
    public ApiResponse<Map<String, Object>> updateForecast(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("id", id, "updated", true, "payload", body));
    }

    @DeleteMapping("/forecasts/{id}")
    public ApiResponse<Map<String, Object>> deleteForecast(@PathVariable String id) {
        return ApiResponse.success(Map.of("id", id, "deleted", true));
    }

    @GetMapping("/reports/investment-match-view")
    public ApiResponse<Map<String, Object>> investmentMatchView(@RequestParam(defaultValue = "103") String resourceId) {
        return ApiResponse.success(ResponseTextSanitizer.cleanMap(analysisService.investmentMatch(resourceId)));
    }

    @GetMapping("/investment-matches")
    public ApiResponse<Map<String, Object>> investmentMatches(@RequestParam(defaultValue = "103") String resourceId) {
        return ApiResponse.success(ResponseTextSanitizer.cleanMap(analysisService.investmentMatch(resourceId)));
    }

    @PostMapping("/investment-matches")
    public ApiResponse<Map<String, Object>> createInvestmentMatch(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("created", true, "payload", body));
    }

    @PutMapping("/investment-matches/{id}")
    public ApiResponse<Map<String, Object>> updateInvestmentMatch(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("id", id, "updated", true, "payload", body));
    }

    @DeleteMapping("/investment-matches/{id}")
    public ApiResponse<Map<String, Object>> deleteInvestmentMatch(@PathVariable String id) {
        return ApiResponse.success(Map.of("id", id, "deleted", true));
    }
}
