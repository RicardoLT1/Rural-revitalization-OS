package com.xiangyun.operation;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.SecurityHeaders;
import com.xiangyun.common.dto.OperationStats;
import com.xiangyun.common.dto.ResourceSummary;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OperationController {

    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/villages")
    public ApiResponse<List<Map<String, Object>>> villages() {
        return ApiResponse.success(operationService.villages());
    }

    @PostMapping("/villages")
    public ApiResponse<Map<String, Object>> createVillage(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("created", true, "payload", body));
    }

    @PutMapping("/villages/{id}")
    public ApiResponse<Map<String, Object>> updateVillage(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("id", id, "updated", true, "payload", body));
    }

    @DeleteMapping("/villages/{id}")
    public ApiResponse<Map<String, Object>> deleteVillage(@PathVariable String id) {
        return ApiResponse.success(Map.of("id", id, "deleted", true));
    }

    @PostMapping("/villages/{id}/enable")
    public ApiResponse<Map<String, Object>> enableVillage(@PathVariable String id) {
        return ApiResponse.success(Map.of("id", id, "enabled", true));
    }

    @PostMapping("/villages/{id}/disable")
    public ApiResponse<Map<String, Object>> disableVillage(@PathVariable String id) {
        return ApiResponse.success(Map.of("id", id, "enabled", false));
    }

    @GetMapping("/resources")
    public ApiResponse<List<ResourceView>> resources(@RequestParam(required = false) String category,
                                                     @RequestParam(required = false) String investmentStatus,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false, defaultValue = "1") Integer page,
                                                     @RequestParam(required = false, defaultValue = "10") Integer size) {
        String actualStatus = investmentStatus != null ? investmentStatus : status;
        return ApiResponse.success(operationService.resources(category, actualStatus, keyword, page, size));
    }

    @GetMapping("/resources/map-points")
    public ApiResponse<List<ResourceView>> mapPoints(@RequestParam(required = false) String category) {
        return ApiResponse.success(operationService.resources(category, null, null));
    }

    @GetMapping("/resources/{id}")
    public ApiResponse<ResourceView> resourceDetail(@PathVariable String id) {
        return ApiResponse.success(operationService.detail(id));
    }

    @PostMapping("/resources")
    public ApiResponse<Map<String, Object>> createResource(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(operationService.createResource(body));
    }

    @PutMapping("/resources/{id}")
    public ApiResponse<Map<String, Object>> updateResource(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(operationService.updateResource(id, body));
    }

    @DeleteMapping("/resources/{id}")
    public ApiResponse<Map<String, Object>> deleteResource(@PathVariable String id) {
        return ApiResponse.success(operationService.deleteResource(id));
    }

    @GetMapping("/resource-tags")
    public ApiResponse<List<String>> tags() {
        return ApiResponse.success(operationService.tags());
    }

    @PostMapping("/resource-tags")
    public ApiResponse<Map<String, Object>> createTag(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("created", true, "payload", body));
    }

    @PutMapping("/resource-tags/{id}")
    public ApiResponse<Map<String, Object>> updateTag(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("id", id, "updated", true, "payload", body));
    }

    @DeleteMapping("/resource-tags/{id}")
    public ApiResponse<Map<String, Object>> deleteTag(@PathVariable String id) {
        return ApiResponse.success(Map.of("id", id, "deleted", true));
    }

    @PostMapping("/resources/{id}/tags/{tagId}")
    public ApiResponse<Map<String, Object>> bindTag(@PathVariable String id, @PathVariable String tagId) {
        return ApiResponse.success(Map.of("resourceId", id, "tagId", tagId, "bound", true));
    }

    @DeleteMapping("/resources/{id}/tags/{tagId}")
    public ApiResponse<Map<String, Object>> unbindTag(@PathVariable String id, @PathVariable String tagId) {
        return ApiResponse.success(Map.of("resourceId", id, "tagId", tagId, "bound", false));
    }

    @PostMapping("/resources/{id}/publish")
    public ApiResponse<Map<String, Object>> publish(@PathVariable String id) {
        return ApiResponse.success(operationService.publishResource(id));
    }

    @PostMapping("/resources/{id}/offline")
    public ApiResponse<Map<String, Object>> offline(@PathVariable String id) {
        return ApiResponse.success(operationService.offlineResource(id));
    }

    @PostMapping("/resources/{id}/investment-status")
    public ApiResponse<Map<String, Object>> updateInvestmentStatus(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(operationService.updateInvestmentStatus(id, String.valueOf(body.getOrDefault("investmentStatus", "洽谈中"))));
    }

    @GetMapping("/operation/reports/weekly")
    public ApiResponse<List<Map<String, Object>>> weeklyReports() {
        return ApiResponse.success(operationService.weeklyReports());
    }

    @PostMapping("/operation/reports/weekly")
    public ApiResponse<Map<String, Object>> createWeeklyReport(
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "") String authorId,
            @RequestHeader(value = SecurityHeaders.USERNAME, defaultValue = "") String authorName,
            @RequestHeader(value = SecurityHeaders.VILLAGE_ID, defaultValue = "1") String villageId,
            @RequestBody Map<String, Object> body) {
        return ApiResponse.success(operationService.createWeeklyReport(body, authorId, authorName, villageId));
    }

    @GetMapping("/resources/{id}/applications/count")
    public ApiResponse<Map<String, Object>> resourceApplicationCount(@PathVariable String id) {
        return ApiResponse.success(operationService.resourceApplicationCount(id));
    }

    @GetMapping("/workflows/workbench")
    public ApiResponse<Map<String, Object>> workbench(@RequestParam(required = false) String category) {
        return ApiResponse.success(operationService.workbench(category));
    }

    @GetMapping("/workflows/processes/{id}")
    public ApiResponse<WorkflowView> workflow(@PathVariable String id) {
        return ApiResponse.success(operationService.workflow(id));
    }

    @GetMapping("/workflows/{id}")
    public ApiResponse<WorkflowView> workflowAlias(@PathVariable String id) {
        return ApiResponse.success(operationService.workflow(id));
    }

    @GetMapping("/workflows/{id}/operation-logs")
    public ApiResponse<List<Map<String, Object>>> workflowOperationLogs(@PathVariable String id) {
        return ApiResponse.success(operationService.operationLogs(id));
    }

    @PostMapping("/workflows/cooperation-applications")
    public ApiResponse<Map<String, Object>> submitCooperationApplication(
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "1") String applicantUserId,
            @RequestHeader(value = SecurityHeaders.USERNAME, defaultValue = "user_demo") String applicantName,
            @RequestHeader(value = "Idempotency-Key", required = false) String requestId,
            @RequestBody Map<String, Object> body) {
        return ApiResponse.success(operationService.submitCooperationApplication(applicantUserId, applicantName, requestId, body));
    }

    @GetMapping("/workflows/my")
    public ApiResponse<List<Map<String, Object>>> myApplications(
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "1") String applicantUserId) {
        return ApiResponse.success(operationService.myApplications(applicantUserId));
    }

    @PostMapping("/workflows/processes")
    public ApiResponse<Map<String, Object>> startWorkflow(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("processId", "wf-" + System.currentTimeMillis(), "started", true, "payload", body));
    }

    @PutMapping("/workflows/processes/{id}")
    public ApiResponse<Map<String, Object>> updateWorkflow(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("processId", id, "updated", true, "payload", body));
    }

    @DeleteMapping("/workflows/processes/{id}")
    public ApiResponse<Map<String, Object>> deleteWorkflow(@PathVariable String id) {
        return ApiResponse.success(Map.of("processId", id, "deleted", true));
    }

    @PostMapping("/workflows/processes/{id}/start")
    public ApiResponse<Map<String, Object>> startProcess(@PathVariable String id) {
        return ApiResponse.success(Map.of("processId", id, "status", "started"));
    }

    @PostMapping("/workflows/processes/{id}/archive")
    public ApiResponse<Map<String, Object>> archiveProcess(@PathVariable String id) {
        return ApiResponse.success(Map.of("processId", id, "archived", true));
    }

    @PostMapping("/workflows/processes/{id}/actions")
    public ApiResponse<Map<String, Object>> workflowAction(
            @PathVariable String id,
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "2") String approverId,
            @RequestHeader(value = SecurityHeaders.USERNAME, defaultValue = "staff_demo") String approverName,
            @RequestBody Map<String, Object> body) {
        return ApiResponse.success(operationService.approve(id, String.valueOf(body.getOrDefault("action", "pass")), approverId, approverName, body));
    }

    @PostMapping("/workflows/{id}/approve")
    public ApiResponse<Map<String, Object>> approveWorkflow(
            @PathVariable String id,
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "2") String approverId,
            @RequestHeader(value = SecurityHeaders.USERNAME, defaultValue = "staff_demo") String approverName,
            @RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(operationService.approve(id, "approve", approverId, approverName, body == null ? Map.of() : body));
    }

    @PostMapping("/workflows/{id}/reject")
    public ApiResponse<Map<String, Object>> rejectWorkflow(
            @PathVariable String id,
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "2") String approverId,
            @RequestHeader(value = SecurityHeaders.USERNAME, defaultValue = "staff_demo") String approverName,
            @RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(operationService.approve(id, "reject", approverId, approverName, body == null ? Map.of() : body));
    }

    @PostMapping("/workflows/{id}/materials")
    public ApiResponse<Map<String, Object>> supplementWorkflowMaterials(
            @PathVariable String id,
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "1") String operatorId,
            @RequestHeader(value = SecurityHeaders.USERNAME, defaultValue = "user_demo") String operatorName,
            @RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(operationService.submitSupplementMaterials(id, operatorId, operatorName, body == null ? Map.of() : body));
    }

    @GetMapping("/workflows/todos")
    public ApiResponse<Object> todos() {
        return ApiResponse.success(operationService.workbench(null).get("filteredTodos"));
    }

    @PostMapping("/todos")
    public ApiResponse<Map<String, Object>> createTodo(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(Map.of("created", true, "payload", body));
    }

    @PostMapping("/todos/{id}/complete")
    public ApiResponse<Map<String, Object>> completeTodo(@PathVariable String id) {
        return ApiResponse.success(Map.of("id", id, "status", "completed"));
    }

    @GetMapping("/workflows/approvals")
    public ApiResponse<Object> approvals(
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "2") String userId,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "STAFF") String role) {
        return ApiResponse.success(operationService.approvalHistory(userId, "ADMIN".equals(role)));
    }

    @PostMapping("/workflows/approvals/{id}/pass")
    public ApiResponse<Map<String, Object>> passApproval(
            @PathVariable String id,
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "2") String approverId,
            @RequestHeader(value = SecurityHeaders.USERNAME, defaultValue = "staff_demo") String approverName) {
        return ApiResponse.success(operationService.approve(id, "approve", approverId, approverName, Map.of()));
    }

    @PostMapping("/workflows/approvals/{id}/reject")
    public ApiResponse<Map<String, Object>> rejectApproval(
            @PathVariable String id,
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "2") String approverId,
            @RequestHeader(value = SecurityHeaders.USERNAME, defaultValue = "staff_demo") String approverName) {
        return ApiResponse.success(operationService.approve(id, "reject", approverId, approverName, Map.of()));
    }

    @GetMapping("/workflows/archives")
    public ApiResponse<List<Map<String, Object>>> archives() {
        return ApiResponse.success(List.of(Map.of("archiveNo", "ARCH-DEMO-001", "status", "archived")));
    }

    @GetMapping("/internal/operation/stats")
    public ApiResponse<OperationStats> stats() {
        return ApiResponse.success(operationService.stats());
    }

    @GetMapping("/internal/resources/{id}/summary")
    public ApiResponse<ResourceSummary> resourceSummary(@PathVariable String id) {
        return ApiResponse.success(operationService.resourceSummary(id));
    }
}
