package com.xiangyun.os.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("investment_match_record")
public class InvestmentMatchRecord extends BaseEntity {

    private Long resourceId;
    private String investor;
    private Integer score;
    private String reason;
    private String priority;
    private String direction;
    private String status;
}
