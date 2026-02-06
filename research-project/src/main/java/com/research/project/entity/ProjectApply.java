package com.research.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目加入申请（科研人员提交 project_no + userId，负责人审批）
 */
@Data
@TableName("project_apply")
public class ProjectApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private Long userId;
    /** PENDING/APPROVED/REJECTED */
    private String status;
    private String applyReason;
    private String replyRemark;
    private Long replyBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
