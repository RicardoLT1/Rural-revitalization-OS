package com.xiangyun.analysis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
public class ReportProperties {

    @Value("${report.default-range-days:${xiangyun.report.default-range-days:7}}")
    private Integer defaultRangeDays;

    public Integer getDefaultRangeDays() {
        return defaultRangeDays == null ? 7 : defaultRangeDays;
    }
}
