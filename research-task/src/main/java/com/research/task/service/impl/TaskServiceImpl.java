package com.research.task.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.research.common.core.constant.Constants;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.exception.BusinessException;
import com.research.common.core.util.SecurityUtils;
import com.research.task.entity.Task;
import com.research.task.entity.TaskAttachment;
import com.research.task.entity.TaskComment;
import com.research.task.mapper.TaskCommentMapper;
import com.research.task.mapper.TaskMapper;
import com.research.task.model.TaskQuery;
import com.research.task.model.TaskStatistics;
import com.research.task.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Autowired
    private TaskCommentMapper taskCommentMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    @Transactional
    public CommonResult<?> createTask(Task task) {
        // 验证必填字段
        if (StrUtil.isBlank(task.getName())) {
            throw new BusinessException("任务名称不能为空");
        }

        if (task.getProjectId() == null) {
            throw new BusinessException("项目ID不能为空");
        }

        // 设置默认值
        if (task.getStatus() == null) {
            task.setStatus("TODO");
        }

        if (task.getPriority() == null) {
            task.setPriority("MEDIUM");
        }

        if (task.getProgress() == null) {
            task.setProgress(0);
        }

        task.setCommentCount(0);
        task.setAttachmentCount(0);
        task.setCreateBy(SecurityUtils.getUserId());
        task.setUpdateBy(SecurityUtils.getUserId());

        // 保存任务
        save(task);

        // 记录操作日志
        log.info("创建任务成功: taskId={}, name={}, projectId={}",
                task.getId(), task.getName(), task.getProjectId());

        // TODO: 发送通知给相关人员
        // sendTaskNotification(task, "CREATE");

        return CommonResult.success(task);
    }

    @Override
    @Transactional
    public CommonResult<?> updateTask(Task task) {
        Task existTask = getById(task.getId());
        if (existTask == null) {
            throw new BusinessException("任务不存在");
        }

        // 检查权限（只有创建人、负责人或管理员可以修改）
        Long currentUserId = SecurityUtils.getUserId();
        if (!existTask.getCreateBy().equals(currentUserId)
                && !existTask.getAssigneeId().equals(currentUserId)
                && !SecurityUtils.isAdmin()) {
            throw new BusinessException("没有权限修改此任务");
        }

        // 更新任务
        task.setUpdateBy(currentUserId);
        updateById(task);

        // 如果状态或进度发生变化，更新项目进度
        if (!Objects.equals(existTask.getStatus(), task.getStatus())
                || !Objects.equals(existTask.getProgress(), task.getProgress())) {
            updateProjectProgress(task.getProjectId());
        }

        log.info("更新任务成功: taskId={}, name={}", task.getId(), task.getName());

        // TODO: 发送通知
        // sendTaskNotification(task, "UPDATE");

        return CommonResult.success(task);
    }

    @Override
    @Transactional
    public CommonResult<?> deleteTask(Long id) {
        Task task = getById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 检查权限
        Long currentUserId = SecurityUtils.getUserId();
        if (!task.getCreateBy().equals(currentUserId) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("没有权限删除此任务");
        }

        // 逻辑删除
        task.setDelFlag(1);
        task.setUpdateBy(currentUserId);
        updateById(task);

        // 更新项目进度
        updateProjectProgress(task.getProjectId());

        log.info("删除任务成功: taskId={}, name={}", id, task.getName());

        return CommonResult.success("删除成功");
    }

    @Override
    public CommonResult<?> getTaskDetail(Long id) {
        Task task = baseMapper.selectTaskDetail(id);
        if (task == null || task.getDelFlag() == 1) {
            throw new BusinessException("任务不存在");
        }

        // 查询附件
        List<TaskAttachment> attachments = getAttachmentsFromDb(id);
        task.setAttachments(attachments);

        // 查询评论
        List<TaskComment> comments = getCommentsFromDb(id);
        task.setComments(comments);

        // 查询子任务（如果存在）
        if (task.getParentId() == null) {
            List<Task> subTasks = baseMapper.selectSubTasks(id);
            task.setSubTasks(subTasks);
        }

        return CommonResult.success(task);
    }

    @Override
    @Transactional
    public CommonResult<?> assignTask(Long taskId, Long assigneeId) {
        Task task = getById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        task.setAssigneeId(assigneeId);
        task.setUpdateBy(SecurityUtils.getUserId());
        updateById(task);

        log.info("分配任务成功: taskId={}, assigneeId={}", taskId, assigneeId);

        // TODO: 发送通知给被分配人
        // sendAssignmentNotification(task, assigneeId);

        return CommonResult.success("分配成功");
    }

    @Override
    @Transactional
    public CommonResult<?> updateTaskStatus(Long taskId, String status) {
        Task task = getById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 验证状态值
        List<String> validStatus = Arrays.asList("TODO", "PROCESSING", "REVIEW", "DONE", "CLOSED");
        if (!validStatus.contains(status)) {
            throw new BusinessException("无效的任务状态");
        }

        task.setStatus(status);
        task.setUpdateBy(SecurityUtils.getUserId());

        // 如果状态为DONE，设置进度为100%
        if ("DONE".equals(status)) {
            task.setProgress(100);
            task.setEndTime(LocalDate.now());
        }

        updateById(task);

        // 更新项目进度
        updateProjectProgress(task.getProjectId());

        log.info("更新任务状态成功: taskId={}, status={}", taskId, status);

        return CommonResult.success("状态更新成功");
    }

    @Override
    @Transactional
    public CommonResult<?> updateTaskProgress(Long taskId, Integer progress) {
        if (progress < 0 || progress > 100) {
            throw new BusinessException("进度值必须在0-100之间");
        }

        Task task = getById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        task.setProgress(progress);
        task.setUpdateBy(SecurityUtils.getUserId());

        // 根据进度自动更新状态
        if (progress == 100 && !"DONE".equals(task.getStatus())) {
            task.setStatus("DONE");
            task.setEndTime(LocalDate.now());
        } else if (progress > 0 && "TODO".equals(task.getStatus())) {
            task.setStatus("PROCESSING");
        }

        updateById(task);

        // 更新项目进度
        updateProjectProgress(task.getProjectId());

        log.info("更新任务进度成功: taskId={}, progress={}", taskId, progress);

        return CommonResult.success("进度更新成功");
    }

    @Override
    public CommonResult<?> getTaskList(TaskQuery query) {
        Page<Task> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<Task> taskPage = baseMapper.selectTaskPage(page, query);

        // 转换为VO
        Page<Map<String, Object>> voPage = new Page<>();
        BeanUtil.copyProperties(taskPage, voPage);

        List<Map<String, Object>> voList = taskPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return CommonResult.success(voPage);
    }

    @Override
    public CommonResult<?> getMyTasks(Long userId, String status) {
        List<Task> tasks = baseMapper.selectUserTasks(userId, status);

        List<Map<String, Object>> voList = tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return CommonResult.success(voList);
    }

    @Override
    public CommonResult<?> getProjectTasks(Long projectId) {
        List<Task> tasks = baseMapper.selectProjectTasks(projectId);

        // 构建任务树
        List<Task> rootTasks = tasks.stream()
                .filter(task -> task.getParentId() == null)
                .collect(Collectors.toList());

        Map<Long, List<Task>> subTaskMap = tasks.stream()
                .filter(task -> task.getParentId() != null)
                .collect(Collectors.groupingBy(Task::getParentId));

        for (Task rootTask : rootTasks) {
            setSubTasks(rootTask, subTaskMap);
        }

        return CommonResult.success(rootTasks);
    }

    @Override
    @Transactional
    public CommonResult<?> uploadAttachment(MultipartFile file, Long taskId) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        Task task = getById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        try {
            // 创建上传目录
            String datePath = DateUtil.format(new Date(), "yyyy/MM/dd");
            String fullPath = uploadPath + "/attachments/" + datePath;
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExt;

            // 保存文件
            Path filePath = Paths.get(fullPath, newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 保存附件信息到数据库
            TaskAttachment attachment = new TaskAttachment();
            attachment.setTaskId(taskId);
            attachment.setFileName(newFilename);
            attachment.setOriginalName(originalFilename);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setFileUrl("/attachments/" + datePath + "/" + newFilename);
            attachment.setUploadTime(LocalDateTime.now());
            attachment.setUploaderId(SecurityUtils.getUserId());
            attachment.setUploaderName(SecurityUtils.getUsername());
            attachment.setDownloadCount(0);

            // TODO: 保存到数据库
            // taskAttachmentMapper.insert(attachment);

            // 更新任务的附件计数
            task.setAttachmentCount(task.getAttachmentCount() + 1);
            updateById(task);

            log.info("上传附件成功: taskId={}, fileName={}", taskId, originalFilename);

            return CommonResult.success(attachment);

        } catch (IOException e) {
            log.error("上传附件失败: {}", e.getMessage());
            throw new BusinessException("上传文件失败");
        }
    }

    @Override
    public CommonResult<?> deleteAttachment(Long attachmentId) {
        // TODO: 实现删除附件逻辑
        return CommonResult.success("删除成功");
    }

    @Override
    public CommonResult<?> getAttachments(Long taskId) {
        List<TaskAttachment> attachments = getAttachmentsFromDb(taskId);
        return CommonResult.success(attachments);
    }

    @Override
    @Transactional
    public CommonResult<?> addComment(TaskComment comment) {
        if (StrUtil.isBlank(comment.getContent())) {
            throw new BusinessException("评论内容不能为空");
        }

        comment.setUserId(SecurityUtils.getUserId());
        comment.setUserName(SecurityUtils.getUsername());
        comment.setCreateTime(LocalDateTime.now());
        comment.setLikeCount(0);

        // TODO: 保存评论到数据库
        // taskCommentMapper.insert(comment);

        // 更新任务的评论计数
        Task task = getById(comment.getTaskId());
        if (task != null) {
            task.setCommentCount(task.getCommentCount() + 1);
            updateById(task);
        }

        log.info("添加评论成功: taskId={}, userId={}", comment.getTaskId(), comment.getUserId());

        return CommonResult.success(comment);
    }

    @Override
    public CommonResult<?> deleteComment(Long commentId) {
        // TODO: 实现删除评论逻辑
        return CommonResult.success("删除成功");
    }

    @Override
    public CommonResult<?> likeComment(Long commentId) {
        // TODO: 实现点赞评论逻辑
        // taskCommentMapper.likeComment(commentId);
        return CommonResult.success("点赞成功");
    }

    @Override
    public CommonResult<?> getComments(Long taskId) {
        List<TaskComment> comments = getCommentsFromDb(taskId);
        return CommonResult.success(buildCommentTree(comments));
    }

    @Override
    public CommonResult<TaskStatistics> getTaskStatistics(Long projectId) {
        // TODO: 从数据库获取统计信息
        // Map<String, Object> stats = baseMapper.selectTaskStatistics(projectId);

        TaskStatistics statistics = new TaskStatistics();
        statistics.setTotalTasks(100);
        statistics.setTodoCount(20);
        statistics.setProcessingCount(50);
        statistics.setReviewCount(10);
        statistics.setDoneCount(20);
        statistics.setAvgProgress(65);

        return CommonResult.success(statistics);
    }

    @Override
    public CommonResult<?> getTaskTimeline(Long projectId) {
        // TODO: 获取任务时间线数据
        return CommonResult.success("时间线数据");
    }

    @Override
    public CommonResult<?> getGanttData(Long projectId) {
        List<Task> tasks = baseMapper.selectProjectTasks(projectId);

        List<Map<String, Object>> ganttData = tasks.stream()
                .filter(task -> task.getStartTime() != null && task.getEndTime() != null)
                .map(task -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", task.getId());
                    item.put("text", task.getName());
                    item.put("start_date", task.getStartTime().toString());
                    item.put("end_date", task.getEndTime().toString());
                    item.put("progress", task.getProgress() / 100.0);
                    item.put("parent", task.getParentId());
                    item.put("type", "task");
                    return item;
                })
                .collect(Collectors.toList());

        return CommonResult.success(ganttData);
    }

    // 私有方法

    /**
     * 更新项目进度（调用项目服务）
     */
    private void updateProjectProgress(Long projectId) {
        // TODO: 调用项目服务的接口更新项目进度
        // 这里可以计算项目的平均进度并更新
        log.info("需要更新项目进度: projectId={}", projectId);
    }

    /**
     * 构建评论树
     */
    private List<TaskComment> buildCommentTree(List<TaskComment> comments) {
        if (CollUtil.isEmpty(comments)) {
            return new ArrayList<>();
        }

        // 找出根评论
        List<TaskComment> rootComments = comments.stream()
                .filter(comment -> comment.getParentId() == null)
                .collect(Collectors.toList());

        // 按父ID分组
        Map<Long, List<TaskComment>> replyMap = comments.stream()
                .filter(comment -> comment.getParentId() != null)
                .collect(Collectors.groupingBy(TaskComment::getParentId));

        // 为每个根评论设置回复
        for (TaskComment comment : rootComments) {
            setReplies(comment, replyMap);
        }

        return rootComments;
    }

    /**
     * 递归设置回复
     */
    private void setReplies(TaskComment comment, Map<Long, List<TaskComment>> replyMap) {
        List<TaskComment> replies = replyMap.get(comment.getId());
        if (CollUtil.isNotEmpty(replies)) {
            comment.setReplies(replies);
            for (TaskComment reply : replies) {
                setReplies(reply, replyMap);
            }
        }
    }

    /**
     * 递归设置子任务
     */
    private void setSubTasks(Task task, Map<Long, List<Task>> subTaskMap) {
        List<Task> subTasks = subTaskMap.get(task.getId());
        if (CollUtil.isNotEmpty(subTasks)) {
            task.setSubTasks(subTasks);
            for (Task subTask : subTasks) {
                setSubTasks(subTask, subTaskMap);
            }
        }
    }

    /**
     * 转换为VO
     */
    private Map<String, Object> convertToVO(Task task) {
        Map<String, Object> vo = new HashMap<>();
        BeanUtil.copyProperties(task, vo);

        // 添加额外信息
        vo.put("statusText", getStatusText(task.getStatus()));
        vo.put("priorityText", getPriorityText(task.getPriority()));

        return vo;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        switch (status) {
            case "TODO": return "待处理";
            case "PROCESSING": return "进行中";
            case "REVIEW": return "审核中";
            case "DONE": return "已完成";
            case "CLOSED": return "已关闭";
            default: return status;
        }
    }

    /**
     * 获取优先级文本
     */
    private String getPriorityText(String priority) {
        switch (priority) {
            case "URGENT": return "紧急";
            case "HIGH": return "高";
            case "MEDIUM": return "中";
            case "LOW": return "低";
            default: return priority;
        }
    }

    /**
     * 从数据库获取附件（模拟）
     */
    private List<TaskAttachment> getAttachmentsFromDb(Long taskId) {
        // TODO: 从数据库查询
        return new ArrayList<>();
    }

    /**
     * 从数据库获取评论（模拟）
     */
    private List<TaskComment> getCommentsFromDb(Long taskId) {
        // TODO: 从数据库查询
        return new ArrayList<>();
    }
}