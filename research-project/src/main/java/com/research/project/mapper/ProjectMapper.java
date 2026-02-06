package com.research.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.research.project.entity.Project;
import com.research.project.model.ProjectQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 项目 Mapper
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    IPage<Project> selectProjectPage(Page<Project> page, @Param("query") ProjectQuery query);

    Project selectProjectDetail(@Param("id") Long id);

    Map<String, Object> selectProjectStatistics(@Param("leaderId") Long leaderId);

    int countTaskByProjectId(@Param("projectId") Long projectId);

    int countDocumentByProjectId(@Param("projectId") Long projectId);

    /** 统计公开项目的公开文档数量 */
    int countPublicDocumentsByProjectIds(@Param("projectIds") List<Long> projectIds);

    /** 立项审核列表：负责人可见，带申请人姓名 */
    IPage<Project> selectAuditPage(Page<Project> page, @Param("leaderId") Long leaderId, @Param("statuses") List<String> statuses);
}
