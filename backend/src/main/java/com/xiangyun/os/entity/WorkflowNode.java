package com.xiangyun.os.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("workflow_node")
public class WorkflowNode extends BaseEntity {

    private Long workflowId;
    private String nodeKey;
    private String title;
    private String status;
    private Integer sortNo;
    private String assignee;
    private String remark;
}
