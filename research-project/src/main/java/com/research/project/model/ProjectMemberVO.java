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
    private String role;
    private String joinTime;
}
