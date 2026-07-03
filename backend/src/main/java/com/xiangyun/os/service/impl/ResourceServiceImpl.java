package com.xiangyun.os.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyun.os.entity.Resource;
import com.xiangyun.os.entity.ResourceTag;
import com.xiangyun.os.entity.ResourceTagRel;
import com.xiangyun.os.exception.BusinessException;
import com.xiangyun.os.mapper.ResourceMapper;
import com.xiangyun.os.mapper.ResourceTagMapper;
import com.xiangyun.os.mapper.ResourceTagRelMapper;
import com.xiangyun.os.service.ResourceService;
import com.xiangyun.os.vo.ResourceDetailVO;
import com.xiangyun.os.vo.ResourceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;
    private final ResourceTagMapper resourceTagMapper;
    private final ResourceTagRelMapper resourceTagRelMapper;

    @Override
    public List<ResourceVO> listResources(String category, String investmentStatus, String keyword, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<Resource>()
                .eq(Resource::getDeleted, 0)
                .orderByDesc(Resource::getAnnualEstimate)
                .orderByAsc(Resource::getId);
        if (StringUtils.hasText(category) && !"全部".equals(category)) {
            wrapper.eq(Resource::getCategory, category);
        }
        if (StringUtils.hasText(investmentStatus)) {
            wrapper.eq(Resource::getInvestmentStatus, investmentStatus);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like(Resource::getName, keyword).or().like(Resource::getAddress, keyword));
        }

        List<Resource> resources = resourceMapper.selectList(wrapper);
        if (page != null && pageSize != null && page > 0 && pageSize > 0) {
            int from = Math.min((page - 1) * pageSize, resources.size());
            int to = Math.min(from + pageSize, resources.size());
            resources = resources.subList(from, to);
        }
        Map<Long, List<String>> tagMap = loadTagMap(resources);
        return resources.stream().map(item -> toResourceVO(item, tagMap)).collect(Collectors.toList());
    }

    @Override
    public ResourceDetailVO getResourceDetail(Long id) {
        Resource resource = resourceMapper.selectOne(new LambdaQueryWrapper<Resource>()
                .eq(Resource::getId, id)
                .eq(Resource::getDeleted, 0));
        if (resource == null) {
            throw new BusinessException(40400, "资源不存在");
        }
        Map<Long, List<String>> tagMap = loadTagMap(Collections.singletonList(resource));
        ResourceDetailVO vo = new ResourceDetailVO();
        copyBase(resource, vo, tagMap.getOrDefault(resource.getId(), Collections.emptyList()));
        vo.setIntro(resource.getIntro());
        vo.setOwner(resource.getOwner());
        vo.setContact(resource.getContact());
        vo.setRelatedProjects(splitText(resource.getRelatedProjects()));
        vo.setOccupancyRate(resource.getOccupancyRate());
        vo.setExpectedROI(resource.getExpectedRoi());
        return vo;
    }

    private Map<Long, List<String>> loadTagMap(List<Resource> resources) {
        List<Long> resourceIds = resources.stream().map(Resource::getId).filter(Objects::nonNull).toList();
        if (resourceIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ResourceTagRel> rels = resourceTagRelMapper.selectList(new LambdaQueryWrapper<ResourceTagRel>()
                .in(ResourceTagRel::getResourceId, resourceIds));
        List<Long> tagIds = rels.stream().map(ResourceTagRel::getTagId).distinct().toList();
        if (tagIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> tagNameMap = resourceTagMapper.selectList(new LambdaQueryWrapper<ResourceTag>()
                        .in(ResourceTag::getId, tagIds)
                        .eq(ResourceTag::getDeleted, 0))
                .stream()
                .collect(Collectors.toMap(ResourceTag::getId, ResourceTag::getName));
        return rels.stream().collect(Collectors.groupingBy(
                ResourceTagRel::getResourceId,
                Collectors.mapping(rel -> tagNameMap.get(rel.getTagId()), Collectors.filtering(Objects::nonNull, Collectors.toList()))
        ));
    }

    private ResourceVO toResourceVO(Resource resource, Map<Long, List<String>> tagMap) {
        ResourceVO vo = new ResourceVO();
        copyBase(resource, vo, tagMap.getOrDefault(resource.getId(), Collections.emptyList()));
        return vo;
    }

    private void copyBase(Resource resource, ResourceVO vo, List<String> tags) {
        vo.setId(String.valueOf(resource.getId()));
        vo.setName(resource.getName());
        vo.setCategory(resource.getCategory());
        vo.setLat(resource.getLat());
        vo.setLng(resource.getLng());
        vo.setAddress(resource.getAddress());
        vo.setArea(resource.getArea());
        vo.setAnnualEstimate(resource.getAnnualEstimate());
        vo.setInvestmentStatus(resource.getInvestmentStatus());
        vo.setTags(tags);
    }

    private List<String> splitText(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.split("[,，]")).map(String::trim).filter(StringUtils::hasText).toList();
    }
}
