package com.xiangyun.os.service;

import com.xiangyun.os.vo.ReportDashboardVO;

public interface ReportService {

    ReportDashboardVO getReportDashboard(String period);
}
