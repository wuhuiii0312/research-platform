package com.research.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.research.project.entity.ProjectResult;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@org.apache.ibatis.annotations.Mapper
public interface ProjectResultMapper extends BaseMapper<ProjectResult> {
    /**
     * 单个项目的成果统计
     * @param projectId 项目ID
     * @return 统计结果（total、paperCount等字段）
     */
    Map<String, Object> statisticResult(@Param("projectId") Long projectId);
}