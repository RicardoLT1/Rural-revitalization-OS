package com.xiangyun.operation;

import java.util.List;

public record WorkflowView(String id, String title, String status, String currentNodeId, String applicantName, List<Node> nodes, List<Record> records) {
    public record Node(String id, String name, String owner, String status, String remark) {
    }

    public record Record(String id, String nodeId, String operator, String action, String time, String remark) {
    }
}
