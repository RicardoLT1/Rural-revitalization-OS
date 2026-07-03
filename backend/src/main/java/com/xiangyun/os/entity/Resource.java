package com.xiangyun.os.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resource")
public class Resource extends BaseEntity {

    private Long villageId;
    private String name;
    private String category;
    private BigDecimal lat;
    private BigDecimal lng;
    private String address;
    private BigDecimal area;
    private BigDecimal annualEstimate;
    private String investmentStatus;
    private String intro;
    private String owner;
    private String contact;
    private String relatedProjects;
    private Integer occupancyRate;
    private Integer expectedRoi;
    private String status;
}
