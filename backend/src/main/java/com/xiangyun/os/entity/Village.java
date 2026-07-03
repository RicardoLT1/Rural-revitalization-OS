package com.xiangyun.os.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("village")
public class Village extends BaseEntity {

    private String name;
    private String region;
    private String address;
    private String intro;
    private String status;
}
