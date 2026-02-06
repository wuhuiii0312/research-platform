package com.research.project.service;

import com.research.common.core.domain.CommonResult;

/**
 * 项目详情同步服务（文档/成果同步到 project_detail 表）
 */
public interface ProjectDetailService {

    /**
     * 同步文档到项目详情（仅非测试数据）
     */
    CommonResult<?> syncDocument(Long projectId, Long documentId);

    /**
     * 同步成果到项目详情（仅非测试数据）
     */
    CommonResult<?> syncResult(Long projectId, Long resultId);
}
