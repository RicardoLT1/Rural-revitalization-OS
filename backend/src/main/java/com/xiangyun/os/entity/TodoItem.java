package com.xiangyun.os.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("todo_item")
public class TodoItem extends BaseEntity {

    private Long workflowId;
    private String title;
    private String category;
    private String status;
    private LocalDateTime dueDate;
    private String assignee;
}
