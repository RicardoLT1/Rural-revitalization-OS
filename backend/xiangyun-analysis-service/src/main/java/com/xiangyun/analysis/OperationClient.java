package com.xiangyun.analysis;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.dto.OperationStats;
import com.xiangyun.common.dto.ResourceSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "xiangyun-operation-service")
public interface OperationClient {
    @GetMapping("/api/internal/operation/stats")
    ApiResponse<OperationStats> stats();

    @GetMapping("/api/internal/resources/{id}/summary")
    ApiResponse<ResourceSummary> resourceSummary(@PathVariable("id") String id);
}
