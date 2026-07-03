package com.xiangyun.os.controller;

import com.xiangyun.os.common.ApiResponse;
import com.xiangyun.os.service.ResourceService;
import com.xiangyun.os.vo.ResourceDetailVO;
import com.xiangyun.os.vo.ResourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "资源模块")
@RestController
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @Operation(summary = "获取资源列表")
    @GetMapping("/resources")
    public ApiResponse<List<ResourceVO>> listResources(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String investmentStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(resourceService.listResources(category, investmentStatus, keyword, page, pageSize));
    }

    @Operation(summary = "获取资源详情")
    @GetMapping("/resources/{id}")
    public ApiResponse<ResourceDetailVO> getResourceDetail(@Parameter(description = "资源 ID") @PathVariable Long id) {
        return ApiResponse.success(resourceService.getResourceDetail(id));
    }
}
