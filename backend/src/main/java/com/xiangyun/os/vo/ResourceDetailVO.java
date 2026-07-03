package com.xiangyun.os.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceDetailVO extends ResourceVO {

    private String intro;
    private String owner;
    private String contact;
    private List<String> relatedProjects;
    private Integer occupancyRate;
    private Integer expectedROI;
}
