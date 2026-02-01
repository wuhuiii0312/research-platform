package com.research.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务附件
 */
@Data
@TableName("task_attachment")
public class TaskAttachment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String fileName;

    private String originalName;

    private String fileType;

    private Long fileSize;

    private String fileUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    private Long uploaderId;

    private String uploaderName;

    private Integer downloadCount;
}
