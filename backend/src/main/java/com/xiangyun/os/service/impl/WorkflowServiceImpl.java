package com.xiangyun.os.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyun.os.entity.ApprovalRecord;
import com.xiangyun.os.entity.TodoItem;
import com.xiangyun.os.entity.Workflow;
import com.xiangyun.os.entity.WorkflowNode;
import com.xiangyun.os.mapper.ApprovalRecordMapper;
import com.xiangyun.os.mapper.TodoItemMapper;
import com.xiangyun.os.mapper.WorkflowMapper;
import com.xiangyun.os.mapper.WorkflowNodeMapper;
import com.xiangyun.os.service.WorkflowService;
import com.xiangyun.os.vo.CollabWorkbenchVO;
import com.xiangyun.os.vo.CommonVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private final WorkflowMapper workflowMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final TodoItemMapper todoItemMapper;
    private final ApprovalRecordMapper approvalRecordMapper;

    @Override
    public CollabWorkbenchVO getWorkbench(String category) {
        String activeCategory = StringUtils.hasText(category) ? category : "全部";
        List<TodoItem> allTodos = todoItemMapper.selectList(new LambdaQueryWrapper<TodoItem>()
                .eq(TodoItem::getDeleted, 0)
                .orderByAsc(TodoItem::getDueDate));
        List<TodoItem> filteredTodos = "全部".equals(activeCategory)
                ? allTodos
                : allTodos.stream().filter(item -> activeCategory.equals(item.getCategory())).toList();
        List<ApprovalRecord> approvals = approvalRecordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getDeleted, 0)
                .orderByDesc(ApprovalRecord::getHandledAt)
                .last("limit 5"));
        List<Workflow> workflows = workflowMapper.selectList(new LambdaQueryWrapper<Workflow>()
                .eq(Workflow::getDeleted, 0)
                .orderByDesc(Workflow::getUpdatedAt));
        Workflow focus = workflows.isEmpty() ? null : workflows.get(0);

        return new CollabWorkbenchVO(
                buildStats(allTodos),
                approvals.stream().map(this::toApproval).toList(),
                focus == null ? List.of() : loadNodes(focus.getId()),
                focus == null ? "" : focus.getCurrentNodeId(),
                buildMessages(allTodos, approvals),
                categoryOptions(),
                activeCategory,
                filteredTodos.stream().map(this::toTodo).toList()
        );
    }

    private CollabWorkbenchVO.TodoStats buildStats(List<TodoItem> todos) {
        Map<String, Long> statusCount = todos.stream().collect(Collectors.groupingBy(TodoItem::getStatus, Collectors.counting()));
        return new CollabWorkbenchVO.TodoStats(
                statusCount.getOrDefault("已逾期", 0L).intValue(),
                todos.size(),
                statusCount.getOrDefault("已完成", 0L).intValue()
        );
    }

    private List<CollabWorkbenchVO.WorkflowNodeItem> loadNodes(Long workflowId) {
        return workflowNodeMapper.selectList(new LambdaQueryWrapper<WorkflowNode>()
                        .eq(WorkflowNode::getDeleted, 0)
                        .eq(WorkflowNode::getWorkflowId, workflowId)
                        .orderByAsc(WorkflowNode::getSortNo))
                .stream()
                .map(item -> new CollabWorkbenchVO.WorkflowNodeItem(
                        item.getNodeKey(),
                        item.getTitle(),
                        item.getAssignee(),
                        item.getStatus(),
                        item.getUpdatedAt() == null ? "" : item.getUpdatedAt().format(DATE_TIME),
                        item.getRemark() == null ? "" : item.getRemark()))
                .toList();
    }

    private CollabWorkbenchVO.TodoViewItem toTodo(TodoItem item) {
        return new CollabWorkbenchVO.TodoViewItem(
                String.valueOf(item.getId()),
                item.getTitle(),
                item.getDueDate() == null ? "" : item.getDueDate().format(DATE_TIME),
                item.getCategory(),
                item.getStatus(),
                statusClass(item.getStatus()),
                String.valueOf(item.getWorkflowId())
        );
    }

    private CollabWorkbenchVO.ApprovalItem toApproval(ApprovalRecord item) {
        return new CollabWorkbenchVO.ApprovalItem(
                String.valueOf(item.getId()),
                item.getTitle(),
                item.getApplicant(),
                item.getAmount(),
                approvalStatus(item.getStatus()),
                String.valueOf(item.getWorkflowId()),
                item.getHandledAt() == null ? "" : item.getHandledAt().format(DATE_TIME)
        );
    }

    private List<CollabWorkbenchVO.WorkflowMessage> buildMessages(List<TodoItem> todos, List<ApprovalRecord> approvals) {
        List<CollabWorkbenchVO.WorkflowMessage> todoMessages = todos.stream()
                .filter(item -> !"已完成".equals(item.getStatus()))
                .limit(2)
                .map(item -> new CollabWorkbenchVO.WorkflowMessage(
                        "msg-todo-" + item.getId(),
                        item.getTitle() + "待处理",
                        item.getDueDate() == null ? "" : item.getDueDate().format(DATE_TIME),
                        String.valueOf(item.getWorkflowId()),
                        "warning"))
                .toList();
        List<CollabWorkbenchVO.WorkflowMessage> approvalMessages = approvals.stream()
                .limit(1)
                .map(item -> new CollabWorkbenchVO.WorkflowMessage(
                        "msg-approval-" + item.getId(),
                        item.getTitle() + "已更新",
                        item.getHandledAt() == null ? "" : item.getHandledAt().format(DATE_TIME),
                        String.valueOf(item.getWorkflowId()),
                        "info"))
                .toList();
        return java.util.stream.Stream.concat(todoMessages.stream(), approvalMessages.stream()).toList();
    }

    private List<CommonVO.OptionItem> categoryOptions() {
        return List.of(
                new CommonVO.OptionItem("全部", "全部"),
                new CommonVO.OptionItem("项目申报", "项目申报"),
                new CommonVO.OptionItem("资产流转", "资产流转"),
                new CommonVO.OptionItem("活动筹备", "活动筹备"),
                new CommonVO.OptionItem("村民议事", "村民议事")
        );
    }

    private String statusClass(String status) {
        return switch (status) {
            case "已逾期" -> "danger";
            case "进行中" -> "doing";
            case "已完成", "已归档" -> "success";
            default -> "pending";
        };
    }

    private String approvalStatus(String status) {
        if ("已通过".equals(status)) {
            return "已通过";
        }
        if ("已驳回".equals(status)) {
            return "已驳回";
        }
        return "待审批";
    }
}
