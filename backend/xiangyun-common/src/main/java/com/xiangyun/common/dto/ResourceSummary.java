package com.xiangyun.common.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResourceSummary(String id, String name, String category, BigDecimal area, BigDecimal annualEstimate, String investmentStatus, List<String> tags) {
}
