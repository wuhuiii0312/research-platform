package com.research.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目详情同步表（文档/成果列表由 RabbitMQ 同步）
 */
@Data
@TableName("project_detail")
public class ProjectDetail {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String documentIds;
    private String resultIds;
    private LocalDateTime updateTime;
}
