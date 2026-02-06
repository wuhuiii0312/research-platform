package com.research.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.research.common.core.domain.CommonResult;
import com.research.task.entity.Task;
import com.research.task.entity.TaskAttachment;
import com.research.task.entity.TaskComment;
import com.research.task.model.TaskQuery;
import com.research.task.model.TaskStatistics;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskService extends IService<Task> {

    // 任务管理
    CommonResult<?> createTask(Task task);

    CommonResult<?> updateTask(Task task);

    CommonResult<?> deleteTask(Long id);

    /** 按项目ID批量删除任务（项目解散时调用） */
    CommonResult<?> deleteTasksByProjectId(Long projectId);

    CommonResult<?> getTaskDetail(Long id);

    CommonResult<?> assignTask(Long taskId, Long assigneeId);

    CommonResult<?> updateTaskStatus(Long taskId, String status);

    CommonResult<?> updateTaskProgress(Long taskId, Integer progress);

    CommonResult<?> getTaskList(TaskQuery query);

    CommonResult<?> getMyTasks(Long userId, String status);

    CommonResult<?> getProjectTasks(Long projectId);

    // 附件管理
    CommonResult<?> uploadAttachment(MultipartFile file, Long taskId);

    CommonResult<?> deleteAttachment(Long attachmentId);

    CommonResult<?> getAttachments(Long taskId);

    // 评论管理
    CommonResult<?> addComment(TaskComment comment);

    CommonResult<?> deleteComment(Long commentId);

    CommonResult<?> likeComment(Long commentId);

    CommonResult<?> getComments(Long taskId);

    // 统计分析
    CommonResult<TaskStatistics> getTaskStatistics(Long projectId);

    CommonResult<?> getTaskTimeline(Long projectId);

    // 甘特图数据
    CommonResult<?> getGanttData(Long projectId);
}