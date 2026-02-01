package com.research.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.research.common.core.domain.CommonResult;
import com.research.project.entity.Project;
import com.research.project.model.ProjectQuery;
import com.research.project.model.ProjectVO;

/**
 * 项目服务接口
 */
public interface ProjectService extends IService<Project> {

    CommonResult<?> createProject(Project project);

    CommonResult<?> updateProject(Project project);

    CommonResult<?> deleteProject(Long id);

    CommonResult<ProjectVO> getProjectDetail(Long id);

    CommonResult<?> getProjectPage(ProjectQuery query);

    CommonResult<?> getProjectStatistics(Long leaderId);
}
