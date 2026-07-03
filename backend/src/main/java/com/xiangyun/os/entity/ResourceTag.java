package com.xiangyun.os.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resource_tag")
public class ResourceTag extends BaseEntity {

    private String name;
    private String type;
    private Integer sortNo;
    private String status;
}
