package com.research.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目成员（项目内角色：LEADER/MEMBER/VISITOR）
 */
@Data
@TableName("project_member")
public class ProjectMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private Long userId;
    /** 项目内角色：LEADER/MEMBER/VISITOR */
    private String role;
    /** 1=已加入 0=待审批 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;
}
