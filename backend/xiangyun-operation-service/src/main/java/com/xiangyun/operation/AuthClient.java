package com.xiangyun.operation;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.dto.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "xiangyun-auth-service")
public interface AuthClient {
    @GetMapping("/api/internal/users/{id}/summary")
    ApiResponse<UserSummary> userSummary(@PathVariable("id") String id);
}
