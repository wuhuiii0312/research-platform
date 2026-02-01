package com.research.project.controller;

import com.research.common.core.annotation.Log;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.enums.BusinessType;
import com.research.project.entity.Project;
import com.research.project.model.ProjectQuery;
import com.research.project.service.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 项目控制器
 */
@RestController
@RequestMapping("/project")
@Api(tags = "项目管理")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Log(title = "创建项目", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    @ApiOperation("创建项目")
    public CommonResult<?> create(@RequestBody Project project) {
        return projectService.createProject(project);
    }

    @Log(title = "更新项目", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    @ApiOperation("更新项目")
    public CommonResult<?> update(@RequestBody Project project) {
        return projectService.updateProject(project);
    }

    @Log(title = "删除项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除项目")
    public CommonResult<?> delete(@PathVariable Long id) {
        return projectService.deleteProject(id);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("项目详情")
    public CommonResult<?> detail(@PathVariable Long id) {
        return projectService.getProjectDetail(id);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public CommonResult<?> page(ProjectQuery query) {
        return projectService.getProjectPage(query);
    }

    @GetMapping("/statistics")
    @ApiOperation("项目统计")
    public CommonResult<?> statistics(@RequestParam(required = false) Long leaderId) {
        return projectService.getProjectStatistics(leaderId);
    }
}
