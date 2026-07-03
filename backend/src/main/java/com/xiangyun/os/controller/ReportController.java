package com.xiangyun.os.controller;

import com.xiangyun.os.common.ApiResponse;
import com.xiangyun.os.service.ReportService;
import com.xiangyun.os.vo.ReportDashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "智能报表")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "获取智能报表总览")
    @GetMapping("/reports/dashboard")
    public ApiResponse<ReportDashboardVO> getReportDashboard(@RequestParam(required = false, defaultValue = "7d") String period) {
        return ApiResponse.success(reportService.getReportDashboard(period));
    }
}
