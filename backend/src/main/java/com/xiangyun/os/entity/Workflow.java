package com.xiangyun.os.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("workflow")
public class Workflow extends BaseEntity {

    private Long villageId;
    private String title;
    private String category;
    private String status;
    private String currentNodeId;
    private String blocker;
    private String applicant;
}
