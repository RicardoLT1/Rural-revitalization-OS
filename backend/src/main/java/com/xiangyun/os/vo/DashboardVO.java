package com.xiangyun.os.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    private String villageName;
    private String roleName;
    private List<StatItem> stats;
    private TrendSeries trends;
    private List<CommonVO.RiskAlert> risks;
    private List<CommonVO.AiSuggestion> suggestions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatItem {
        private String key;
        private String title;
        private Object value;
        private String unit;
        private Number delta;
        private String trend;
        private String status;
        private String icon;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendSeries {
        private List<CommonVO.TrendPoint> days7;
        private List<CommonVO.TrendPoint> days30;
    }
}
