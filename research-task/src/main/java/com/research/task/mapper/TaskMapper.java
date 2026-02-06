package com.research.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.research.task.entity.Task;
import com.research.task.model.TaskQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TaskMapper extends BaseMapper<Task> {

    // 分页查询任务（仅当前用户参与项目的任务）
    IPage<Task> selectTaskPage(Page<Task> page,
                               @Param("query") TaskQuery query,
                               @Param("userId") Long userId);

    // 查询任务详情
    Task selectTaskDetail(Long id);

    // 查询用户的任务
    List<Task> selectUserTasks(@Param("userId") Long userId, @Param("status") String status);

    // 查询项目下的任务
    List<Task> selectProjectTasks(@Param("projectId") Long projectId);

    // 任务统计
    Map<String, Object> selectTaskStatistics(@Param("projectId") Long projectId);

    // 查询任务的子任务
    List<Task> selectSubTasks(Long parentId);
}