package com.research.project.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.research.common.core.domain.CommonResult;
import com.research.project.entity.ProjectDetail;
import com.research.project.mapper.ProjectDetailMapper;
import com.research.project.service.ProjectDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 项目详情同步服务实现（直接操作 project_detail 表，替代 RabbitMQ）
 */
@Slf4j
@Service
public class ProjectDetailServiceImpl implements ProjectDetailService {

    @Autowired
    private ProjectDetailMapper projectDetailMapper;

    @Override
    @Transactional
    public CommonResult<?> syncDocument(Long projectId, Long documentId) {
        if (projectId == null || documentId == null) {
            return CommonResult.fail(400, "项目ID和文档ID不能为空");
        }
        ProjectDetail detail = getOrCreate(projectId);
        Set<String> ids = new LinkedHashSet<>(
                Arrays.asList(StrUtil.isBlank(detail.getDocumentIds()) ? new String[0] : detail.getDocumentIds().split(","))
        );
        ids.add(String.valueOf(documentId));
        detail.setDocumentIds(ids.stream().filter(s -> !s.isEmpty()).collect(Collectors.joining(",")));
        detail.setUpdateTime(LocalDateTime.now());
        projectDetailMapper.updateById(detail);
        log.info("同步文档到项目详情: projectId={}, documentId={}", projectId, documentId);
        return CommonResult.success("同步成功");
    }

    @Override
    @Transactional
    public CommonResult<?> syncResult(Long projectId, Long resultId) {
        if (projectId == null || resultId == null) {
            return CommonResult.fail(400, "项目ID和成果ID不能为空");
        }
        ProjectDetail detail = getOrCreate(projectId);
        Set<String> ids = new LinkedHashSet<>(
                Arrays.asList(StrUtil.isBlank(detail.getResultIds()) ? new String[0] : detail.getResultIds().split(","))
        );
        ids.add(String.valueOf(resultId));
        detail.setResultIds(ids.stream().filter(s -> !s.isEmpty()).collect(Collectors.joining(",")));
        detail.setUpdateTime(LocalDateTime.now());
        projectDetailMapper.updateById(detail);
        log.info("同步成果到项目详情: projectId={}, resultId={}", projectId, resultId);
        return CommonResult.success("同步成功");
    }

    private ProjectDetail getOrCreate(Long projectId) {
        ProjectDetail d = projectDetailMapper.selectOne(
                new LambdaQueryWrapper<ProjectDetail>().eq(ProjectDetail::getProjectId, projectId));
        if (d != null) return d;
        d = new ProjectDetail();
        d.setProjectId(projectId);
        d.setUpdateTime(LocalDateTime.now());
        projectDetailMapper.insert(d);
        return d;
    }
}