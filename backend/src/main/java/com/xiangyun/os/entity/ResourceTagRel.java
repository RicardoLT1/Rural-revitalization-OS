package com.xiangyun.os.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("resource_tag_rel")
public class ResourceTagRel {

    private Long resourceId;
    private Long tagId;
}
