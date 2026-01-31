package com.research.task.controller;

import com.research.common.core.annotation.Log;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.enums.BusinessType;
import com.research.task.entity.Task;
import com.research.task.entity.TaskComment;
import com.research.task.model.TaskQuery;
import com.research.task.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/task")
@Api(tags = "任务管理")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Log(title = "创建任务", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    @ApiOperation("创建任务")
    public CommonResult<?> createTask(@Valid @RequestBody Task task) {
        return taskService.createTask(task);
    }

    @Log(title = "更新任务", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    @ApiOperation("更新任务")
    public CommonResult<?> updateTask(@Valid @RequestBody Task task) {
        return taskService.updateTask(task);
    }

    @Log(title = "删除任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除任务")
    public CommonResult<?> deleteTask(@PathVariable Long id) {
        return taskService.deleteTask(id);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("获取任务详情")
    public CommonResult<?> getTaskDetail(@PathVariable Long id) {
        return taskService.getTaskDetail(id);
    }

    @Log(title = "分配任务", businessType = BusinessType.UPDATE)
    @PostMapping("/assign")
    @ApiOperation("分配任务")
    public CommonResult<?> assignTask(@RequestParam Long taskId,
                                      @RequestParam Long assigneeId) {
        return taskService.assignTask(taskId, assigneeId);
    }

    @Log(title = "更新任务状态", businessType = BusinessType.UPDATE)
    @PostMapping("/update-status")
    @ApiOperation("更新任务状态")
    public CommonResult<?> updateTaskStatus(@RequestParam Long taskId,
                                            @RequestParam String status) {
        return taskService.updateTaskStatus(taskId, status);
    }

    @Log(title = "更新任务进度", businessType = BusinessType.UPDATE)
    @PostMapping("/update-progress")
    @ApiOperation("更新任务进度")
    public CommonResult<?> updateTaskProgress(@RequestParam Long taskId,
                                              @RequestParam Integer progress) {
        return taskService.updateTaskProgress(taskId, progress);
    }

    @GetMapping("/list")
    @ApiOperation("获取任务列表")
    public CommonResult<?> getTaskList(TaskQuery query) {
        return taskService.getTaskList(query);
    }

    @GetMapping("/my-tasks")
    @ApiOperation("获取我的任务")
    public CommonResult<?> getMyTasks(@RequestParam Long userId,
                                      @RequestParam(required = false) String status) {
        return taskService.getMyTasks(userId, status);
    }

    @GetMapping("/project-tasks/{projectId}")
    @ApiOperation("获取项目任务")
    public CommonResult<?> getProjectTasks(@PathVariable Long projectId) {
        return taskService.getProjectTasks(projectId);
    }

    @Log(title = "上传附件", businessType = BusinessType.INSERT)
    @PostMapping("/upload-attachment")
    @ApiOperation("上传附件")
    public CommonResult<?> uploadAttachment(@RequestParam("file") MultipartFile file,
                                            @RequestParam Long taskId) {
        return taskService.uploadAttachment(file, taskId);
    }

    @Log(title = "删除附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete-attachment/{attachmentId}")
    @ApiOperation("删除附件")
    public CommonResult<?> deleteAttachment(@PathVariable Long attachmentId) {
        return taskService.deleteAttachment(attachmentId);
    }

    @GetMapping("/attachments/{taskId}")
    @ApiOperation("获取任务附件")
    public CommonResult<?> getAttachments(@PathVariable Long taskId) {
        return taskService.getAttachments(taskId);
    }

    @Log(title = "添加评论", businessType = BusinessType.INSERT)
    @PostMapping("/add-comment")
    @ApiOperation("添加评论")
    public CommonResult<?> addComment(@Valid @RequestBody TaskComment comment) {
        return taskService.addComment(comment);
    }

    @Log(title = "删除评论", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete-comment/{commentId}")
    @ApiOperation("删除评论")
    public CommonResult<?> deleteComment(@PathVariable Long commentId) {
        return taskService.deleteComment(commentId);
    }

    @Log(title = "点赞评论", businessType = BusinessType.UPDATE)
    @PostMapping("/like-comment/{commentId}")
    @ApiOperation("点赞评论")
    public CommonResult<?> likeComment(@PathVariable Long commentId) {
        return taskService.likeComment(commentId);
    }

    @GetMapping("/comments/{taskId}")
    @ApiOperation("获取任务评论")
    public CommonResult<?> getComments(@PathVariable Long taskId) {
        return taskService.getComments(taskId);
    }

    @GetMapping("/statistics/{projectId}")
    @ApiOperation("获取任务统计")
    public CommonResult<?> getTaskStatistics(@PathVariable Long projectId) {
        return taskService.getTaskStatistics(projectId);
    }

    @GetMapping("/timeline/{projectId}")
    @ApiOperation("获取任务时间线")
    public CommonResult<?> getTaskTimeline(@PathVariable Long projectId) {
        return taskService.getTaskTimeline(projectId);
    }

    @GetMapping("/gantt-data/{projectId}")
    @ApiOperation("获取甘特图数据")
    public CommonResult<?> getGanttData(@PathVariable Long projectId) {
        return taskService.getGanttData(projectId);
    }
}