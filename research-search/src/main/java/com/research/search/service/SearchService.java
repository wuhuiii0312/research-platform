package com.research.search.service;

import com.research.common.core.domain.CommonResult;

/**
 * 搜索服务接口
 */
public interface SearchService {

    CommonResult<?> searchDocuments(String keyword, String projectId, String type,
                                    Integer pageNum, Integer pageSize);

    CommonResult<?> searchTasks(String keyword, Long projectId, String status,
                                Long assigneeId, Integer pageNum, Integer pageSize);

    CommonResult<?> searchUsers(String keyword, String role, Integer pageNum, Integer pageSize);

    CommonResult<?> getHotKeywords();

    CommonResult<?> rebuildIndex();

    /** 公开检索（is_public=1 且 test_data_flag=false） */
    CommonResult<?> searchPublic(String keyword, String type, Integer pageNum, Integer pageSize);
}
