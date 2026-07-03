package com.xiangyun.os.service;

import com.xiangyun.os.vo.ResourceDetailVO;
import com.xiangyun.os.vo.ResourceVO;

import java.util.List;

public interface ResourceService {

    List<ResourceVO> listResources(String category, String investmentStatus, String keyword, Integer page, Integer pageSize);

    ResourceDetailVO getResourceDetail(Long id);
}
