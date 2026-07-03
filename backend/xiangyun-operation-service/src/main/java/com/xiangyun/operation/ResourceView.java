package com.xiangyun.operation;

import java.math.BigDecimal;
import java.util.List;

public record ResourceView(
        String id,
        String name,
        String category,
        BigDecimal lat,
        BigDecimal lng,
        String address,
        BigDecimal area,
        BigDecimal annualEstimate,
        String investmentStatus,
        List<String> tags,
        String intro,
        String owner,
        String contact,
        List<String> relatedProjects,
        Integer occupancyRate,
        Integer expectedROI
) {
}
