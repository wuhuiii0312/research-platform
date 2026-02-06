package com.research.project.controller;

import com.research.common.core.domain.CommonResult;
import com.research.project.service.ProjectDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 项目详情同步接口（供文档/成果服务调用，替代 RabbitMQ）
 */
@RestController
@RequestMapping("/project/detail")
@Api(tags = "项目详情同步")
public class ProjectDetailController {

    @Autowired
    private ProjectDetailService projectDetailService;

    @PostMapping("/sync/document")
    @ApiOperation("同步文档到项目详情")
    public CommonResult<?> syncDocument(@RequestParam Long projectId,
                                       @RequestParam Long documentId) {
        return projectDetailService.syncDocument(projectId, documentId);
    }

    @PostMapping("/sync/result")
    @ApiOperation("同步成果到项目详情")
    public CommonResult<?> syncResult(@RequestParam Long projectId,
                                      @RequestParam Long resultId) {
        return projectDetailService.syncResult(projectId, resultId);
    }
}
