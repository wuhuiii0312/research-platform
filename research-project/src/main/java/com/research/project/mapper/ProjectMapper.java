package com.research.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.research.project.entity.Project;
import com.research.project.model.ProjectQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 项目 Mapper
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    IPage<Project> selectProjectPage(Page<Project> page, @Param("query") ProjectQuery query);

    Project selectProjectDetail(@Param("id") Long id);

    Map<String, Object> selectProjectStatistics(@Param("leaderId") Long leaderId);
}
