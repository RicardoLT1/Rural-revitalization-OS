package com.xiangyun.os.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report_snapshot")
public class ReportSnapshot extends BaseEntity {

    private Long villageId;
    private LocalDate statDate;
    private Integer visitorCount;
    private BigDecimal revenue;
    private BigDecimal projectProgress;
    private Integer riskCount;
    private BigDecimal investmentConversionRate;
    private BigDecimal cultureRevenue;
    private BigDecimal productRevenue;
    private BigDecimal serviceRevenue;
}
