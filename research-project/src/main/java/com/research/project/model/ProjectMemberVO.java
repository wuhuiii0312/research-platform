package com.research.project.model;

import lombok.Data;

/**
 * 项目成员 VO
 */
@Data
public class ProjectMemberVO {
    private Long userId;
    private String username;
    private String name;
    /** 个人中心展示的 5 位用户ID（inviteCode） */
    private String inviteCode;
    private String role;
    private String joinTime;
}
