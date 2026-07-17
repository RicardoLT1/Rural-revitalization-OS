package com.xiangyun.operation;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.dto.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "xiangyun-auth-service")
public interface AuthClient {
    @GetMapping("/api/internal/users/{id}/summary")
    ApiResponse<UserSummary> userSummary(@PathVariable("id") String id);

    @GetMapping("/api/internal/users/search")
    ApiResponse<List<UserSummary>> searchUsers(@RequestParam("keyword") String keyword,
                                               @RequestParam("villageId") String villageId,
                                               @RequestParam("limit") Integer limit);
}
