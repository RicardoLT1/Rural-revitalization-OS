package com.xiangyun.os.controller;

import com.xiangyun.os.common.ApiResponse;
import com.xiangyun.os.service.WorkflowService;
import com.xiangyun.os.vo.CollabWorkbenchVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "协同工作流")
@RestController
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @Operation(summary = "获取协同工作台总览")
    @GetMapping("/workflows/workbench")
    public ApiResponse<CollabWorkbenchVO> getWorkbench(@RequestParam(required = false) String category) {
        return ApiResponse.success(workflowService.getWorkbench(category));
    }
}
