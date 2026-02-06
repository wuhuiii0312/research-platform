package com.research.project.service;

import com.research.common.core.domain.CommonResult;

/**
 * 成果服务接口当前仅在统计等场景中使用；
 * 详细的 CRUD 能力由后续迭代按需补充实现。
 */
public interface ProjectResultService {

    /**
     * 成果统计：按项目维度统计成果数量等。
     * 具体的「只统计当前用户参与项目」逻辑在控制层中实现，
     * 该接口暂保留以兼容已有调用。
     */
    CommonResult<?> statistic(Long projectId);
}
