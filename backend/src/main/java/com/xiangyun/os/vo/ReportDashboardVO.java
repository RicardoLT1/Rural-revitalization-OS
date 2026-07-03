package com.xiangyun.os.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDashboardVO {

    private List<ReportSummary> summary;
    private List<CommonVO.OptionItem> periods;
    private String period;
    private List<CommonVO.TrendPoint> flowPoints;
    private RevenueBar revenueBar;
    private RevenueRatio ratioRing;
    private String autoSummary;
    private List<CommonVO.AiSuggestion> aiTips;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportSummary {
        private String id;
        private String title;
        private String value;
        private String delta;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueBar {
        private List<String> labels;
        private List<CommonVO.ChartSeries> series;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueRatio {
        private List<String> labels;
        private List<BigDecimal> values;
        private List<String> colors;
    }
}
