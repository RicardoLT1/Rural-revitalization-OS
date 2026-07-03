package com.xiangyun.os.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_record")
public class ApprovalRecord extends BaseEntity {

    private Long workflowId;
    private String nodeId;
    private String title;
    private String applicant;
    private BigDecimal amount;
    private String action;
    private String status;
    private String remark;
    private LocalDateTime handledAt;
}
