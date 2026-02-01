package com.research.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.research.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task")
public class Task extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long parentId;

    private String name;

    private String description;

    private Long assigneeId;

    private Long reporterId;

    private String status;

    private String priority;

    private String type;

    private BigDecimal estimatedHours;

    private BigDecimal actualHours;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueTime;

    private Integer progress;

    private String tags;

    private Integer attachmentCount;

    private Integer commentCount;

    // 非数据库字段
    @TableField(exist = false)
    private String projectName;

    @TableField(exist = false)
    private String assigneeName;

    @TableField(exist = false)
    private String reporterName;

    @TableField(exist = false)
    private List<TaskAttachment> attachments;

    @TableField(exist = false)
    private List<TaskComment> comments;

    @TableField(exist = false)
    private List<Task> subTasks;
}