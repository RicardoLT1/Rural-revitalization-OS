package com.xiangyun.os.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public final class CommonVO {

    private CommonVO() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionItem {
        private String key;
        private String label;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private String date;
        private Number value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiSuggestion {
        private String id;
        private String title;
        private String content;
        private String priority;
        private String actionLabel;
        private String actionType;
        private String tag;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskAlert {
        private String id;
        private String title;
        private String level;
        private String detail;
        private String assignee;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartSeries {
        private String name;
        private List<BigDecimal> values;
    }
}
