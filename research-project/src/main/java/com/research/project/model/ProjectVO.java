package com.research.project.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectVO {
    private Long id;
    /** 5位项目编号，供搜索与申请加入 */
    private Integer projectNo;
    private String name;
    private String code;
    private String description;
    private Long leaderId;
    /** 负责人账号（用户名） */
    private String leaderUsername;
    private String leaderName;
    private String leaderEmail;
    private String status;
    private String priority;
    private BigDecimal budget;
    private LocalDate startTime;
    private LocalDate endTime;
    private LocalDate actualEndTime;
    private Integer progress;
    private String tags;
    private Boolean isPublic;
    private Integer taskCount;
    private Integer memberCount;
    private Integer docCount;
    private Integer achievementCount;
    private LocalDateTime createTime;
    private List<ProjectMemberVO> members;
}