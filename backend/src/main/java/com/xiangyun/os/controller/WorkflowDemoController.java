package com.xiangyun.os.controller;

import com.xiangyun.os.common.ApiResponse;
import com.xiangyun.os.service.WorkflowService;
import com.xiangyun.os.vo.CollabWorkbenchVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Workflow Demo Extension")
@RestController
@RequiredArgsConstructor
public class WorkflowDemoController {

    private final WorkflowService workflowService;

    @Operation(summary = "List workflow todos")
    @GetMapping("/workflows/todos")
    public ApiResponse<List<CollabWorkbenchVO.TodoViewItem>> todos(@RequestParam(required = false) String category) {
        return ApiResponse.success(workflowService.getWorkbench(category).getFilteredTodos());
    }

    @Operation(summary = "List workflow approvals")
    @GetMapping("/workflows/approvals")
    public ApiResponse<List<CollabWorkbenchVO.ApprovalItem>> approvals() {
        return ApiResponse.success(workflowService.getWorkbench(null).getApprovals());
    }

    @Operation(summary = "List workflow categories")
    @GetMapping("/workflows/categories")
    public ApiResponse<List<?>> categories() {
        return ApiResponse.success(workflowService.getWorkbench(null).getCategoryOptions());
    }

    @Operation(summary = "Get process detail demo")
    @GetMapping("/workflows/processes/{id}")
    public ApiResponse<Map<String, Object>> processDetail(@PathVariable String id) {
        CollabWorkbenchVO workbench = workflowService.getWorkbench(null);
        return ApiResponse.success(Map.of(
                "id", id,
                "title", "Resource activation approval process",
                "status", "running",
                "currentNodeId", workbench.getWorkflowStripCurrent(),
                "nodes", workbench.getWorkflowStrip(),
                "records", List.of(
                        Map.of("operator", "village operator", "action", "submit", "time", "2026-06-20 09:30"),
                        Map.of("operator", "platform admin", "action", "review", "time", "2026-06-21 14:20")
                )
        ));
    }

    @Operation(summary = "Get process records")
    @GetMapping("/workflows/processes/{id}/records")
    public ApiResponse<List<Map<String, Object>>> processRecords(@PathVariable String id) {
        return ApiResponse.success(List.of(
                Map.of("processId", id, "nodeId", "submit", "action", "submit", "operator", "village operator"),
                Map.of("processId", id, "nodeId", "review", "action", "approve", "operator", "platform admin")
        ));
    }

    @Operation(summary = "Submit process action")
    @PostMapping("/workflows/processes/{id}/actions")
    public ApiResponse<Map<String, Object>> processAction(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("processId", id, "accepted", true, "action", body));
    }

    @Operation(summary = "List archives")
    @GetMapping("/workflows/archives")
    public ApiResponse<List<Map<String, Object>>> archives(@RequestParam(required = false) String processId) {
        return ApiResponse.success(List.of(
                Map.of("processId", processId == null ? "wf-001" : processId, "archiveNo", "ARCH-2026-001", "status", "archived"),
                Map.of("processId", "wf-002", "archiveNo", "ARCH-2026-002", "status", "reviewing")
        ));
    }

    @Operation(summary = "Start workflow")
    @PostMapping("/workflows/processes")
    public ApiResponse<Map<String, Object>> startProcess(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("processId", "wf-demo-new", "started", true, "payload", body));
    }

    @Operation(summary = "Cancel workflow")
    @PostMapping("/workflows/processes/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancelProcess(@PathVariable String id) {
        return ApiResponse.success(Map.of("processId", id, "status", "cancelled"));
    }

    @Operation(summary = "Get workflow messages")
    @GetMapping("/workflows/messages")
    public ApiResponse<List<CollabWorkbenchVO.WorkflowMessage>> messages() {
        return ApiResponse.success(workflowService.getWorkbench(null).getMessages());
    }
}
