package com.xiangyun.os.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollabWorkbenchVO {

    private TodoStats todoStats;
    private List<ApprovalItem> approvals;
    private List<WorkflowNodeItem> workflowStrip;
    private String workflowStripCurrent;
    private List<WorkflowMessage> messages;
    private List<CommonVO.OptionItem> categoryOptions;
    private String activeCategory;
    private List<TodoViewItem> filteredTodos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodoStats {
        private Integer urgent;
        private Integer total;
        private Integer completedToday;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalItem {
        private String id;
        private String title;
        private String applicant;
        private BigDecimal amount;
        private String status;
        private String processId;
        private String time;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowNodeItem {
        private String id;
        private String name;
        private String owner;
        private String status;
        private String time;
        private String remark;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowMessage {
        private String id;
        private String title;
        private String time;
        private String processId;
        private String level;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodoViewItem {
        private String id;
        private String title;
        private String dueDate;
        private String category;
        private String status;
        private String statusClass;
        private String processId;
    }
}
