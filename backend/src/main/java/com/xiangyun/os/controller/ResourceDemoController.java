package com.xiangyun.os.controller;

import com.xiangyun.os.common.ApiResponse;
import com.xiangyun.os.service.ResourceService;
import com.xiangyun.os.vo.ResourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Resource Demo Extension")
@RestController
@RequiredArgsConstructor
public class ResourceDemoController {

    private final ResourceService resourceService;

    @Operation(summary = "Get map resource points")
    @GetMapping("/resources/map-points")
    public ApiResponse<List<ResourceVO>> mapPoints(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String investmentStatus,
            @RequestParam(required = false) String tag
    ) {
        return ApiResponse.success(resourceService.listResources(category, investmentStatus, tag, null, null));
    }

    @Operation(summary = "Create resource demo")
    @PostMapping("/resources")
    public ApiResponse<Map<String, Object>> createResource(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("id", "demo-resource-new", "created", true, "payload", body));
    }

    @Operation(summary = "Update resource demo")
    @PutMapping("/resources/{id}")
    public ApiResponse<Map<String, Object>> updateResource(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("id", id, "updated", true, "payload", body));
    }

    @Operation(summary = "Delete resource demo")
    @DeleteMapping("/resources/{id}")
    public ApiResponse<Map<String, Object>> deleteResource(@PathVariable Long id) {
        return ApiResponse.success(Map.of("id", id, "deleted", true));
    }

    @Operation(summary = "Get resource tags")
    @GetMapping("/resources/tags")
    public ApiResponse<List<String>> tags() {
        return ApiResponse.success(List.of("all", "idle-house", "farmland", "tourism-space", "investment-ready"));
    }

    @Operation(summary = "Get resource categories")
    @GetMapping("/resources/categories")
    public ApiResponse<List<String>> categories() {
        return ApiResponse.success(List.of("idle-house", "farmland", "tourism", "culture", "service"));
    }

    @Operation(summary = "Get investment matches")
    @GetMapping("/resources/{resourceId}/investment-matches")
    public ApiResponse<List<Map<String, Object>>> investmentMatches(@PathVariable Long resourceId) {
        return ApiResponse.success(List.of(
                Map.of("id", "match-1", "resourceId", resourceId, "investor", "Agri-tourism Partner A", "score", 92, "priority", "high"),
                Map.of("id", "match-2", "resourceId", resourceId, "investor", "Local Product Channel B", "score", 86, "priority", "medium")
        ));
    }

    @Operation(summary = "Publish resource")
    @PostMapping("/resources/{id}/publish")
    public ApiResponse<Map<String, Object>> publish(@PathVariable Long id) {
        return ApiResponse.success(Map.of("id", id, "status", "published"));
    }

    @Operation(summary = "Offline resource")
    @PostMapping("/resources/{id}/offline")
    public ApiResponse<Map<String, Object>> offline(@PathVariable Long id) {
        return ApiResponse.success(Map.of("id", id, "status", "offline"));
    }
}
