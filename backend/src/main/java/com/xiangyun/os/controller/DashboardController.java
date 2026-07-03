package com.xiangyun.os.controller;

import com.xiangyun.os.common.ApiResponse;
import com.xiangyun.os.service.DashboardService;
import com.xiangyun.os.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "首页驾驶舱")
@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取首页驾驶舱总览")
    @GetMapping("/dashboard")
    public ApiResponse<DashboardVO> getDashboard() {
        return ApiResponse.success(dashboardService.getDashboard());
    }

    @Operation(summary = "Get dashboard core stats")
    @GetMapping("/dashboard/stats")
    public ApiResponse<List<DashboardVO.StatItem>> getStats() {
        return ApiResponse.success(dashboardService.getDashboard().getStats());
    }

    @Operation(summary = "Get visitor trend")
    @GetMapping("/dashboard/trends")
    public ApiResponse<List<?>> getTrends(@RequestParam(defaultValue = "7d") String period) {
        DashboardVO.TrendSeries trends = dashboardService.getDashboard().getTrends();
        return ApiResponse.success("30d".equals(period) ? trends.getDays30() : trends.getDays7());
    }

    @Operation(summary = "Get AI suggestions")
    @GetMapping("/dashboard/suggestions")
    public ApiResponse<List<?>> getSuggestions() {
        return ApiResponse.success(dashboardService.getDashboard().getSuggestions());
    }

    @Operation(summary = "Get risk alerts")
    @GetMapping("/dashboard/risks")
    public ApiResponse<List<?>> getRisks() {
        return ApiResponse.success(dashboardService.getDashboard().getRisks());
    }

    @Operation(summary = "Get village profile")
    @GetMapping("/dashboard/village")
    public ApiResponse<Map<String, Object>> getVillage() {
        DashboardVO dashboard = dashboardService.getDashboard();
        return ApiResponse.success(Map.of(
                "name", dashboard.getVillageName(),
                "roleName", dashboard.getRoleName(),
                "operator", "Xiangyun OS Demo Team",
                "scene", "rural revitalization operation cockpit"
        ));
    }
}
