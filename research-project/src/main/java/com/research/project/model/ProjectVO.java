package com.research.project.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Long leaderId;
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
    private Integer taskCount;
    private Integer memberCount;
    private Integer achievementCount;
    private LocalDateTime createTime;
    private List<ProjectMemberVO> members;
}