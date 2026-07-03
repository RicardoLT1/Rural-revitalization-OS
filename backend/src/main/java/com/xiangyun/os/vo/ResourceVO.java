package com.xiangyun.os.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ResourceVO {

    private String id;
    private String name;
    private String category;
    private BigDecimal lat;
    private BigDecimal lng;
    private String address;
    private BigDecimal area;
    private BigDecimal annualEstimate;
    private String investmentStatus;
    private List<String> tags;
}
