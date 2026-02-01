package com.research.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.exception.BusinessException;
import com.research.common.core.util.SecurityUtils;
import com.research.project.entity.Project;
import com.research.project.mapper.ProjectMapper;
import com.research.project.model.ProjectQuery;
import com.research.project.model.ProjectVO;
import com.research.project.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 项目服务实现
 */
@Slf4j
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    @Transactional
    public CommonResult<?> createProject(Project project) {
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new BusinessException("项目名称不能为空");
        }
        project.setProgress(0);
        project.setStatus("INIT");
        project.setCreateBy(SecurityUtils.getUserId());
        project.setUpdateBy(SecurityUtils.getUserId());
        save(project);
        log.info("创建项目成功: id={}, name={}", project.getId(), project.getName());
        return CommonResult.success(project);
    }

    @Override
    @Transactional
    public CommonResult<?> updateProject(Project project) {
        Project exist = getById(project.getId());
        if (exist == null) {
            throw new BusinessException("项目不存在");
        }
        project.setUpdateBy(SecurityUtils.getUserId());
        updateById(project);
        return CommonResult.success("更新成功");
    }

    @Override
    @Transactional
    public CommonResult<?> deleteProject(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        project.setDelFlag(1);
        project.setUpdateBy(SecurityUtils.getUserId());
        updateById(project);
        return CommonResult.success("删除成功");
    }

    @Override
    public CommonResult<ProjectVO> getProjectDetail(Long id) {
        Project project = baseMapper.selectProjectDetail(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        ProjectVO vo = BeanUtil.copyProperties(project, ProjectVO.class);
        return CommonResult.success(vo);
    }

    @Override
    public CommonResult<?> getProjectPage(ProjectQuery query) {
        Page<Project> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<Project> result = baseMapper.selectProjectPage(page, query);
        return CommonResult.success(result);
    }

    @Override
    public CommonResult<?> getProjectStatistics(Long leaderId) {
        return CommonResult.success(baseMapper.selectProjectStatistics(leaderId));
    }
}
