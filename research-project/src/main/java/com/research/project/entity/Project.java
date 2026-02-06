package com.research.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.research.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 科研项目实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project")
public class Project extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 5位项目编号 10000-99999，唯一，供搜索与申请加入 */
    private Integer projectNo;

    private String name;
    @TableField(exist = false)
    private String code;
    private String description;
    private Long leaderId;
    private String status;
    @TableField(exist = false)
    private String priority;
    @TableField(exist = false)
    private BigDecimal budget;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField(exist = false)
    private LocalDate actualEndTime;

    private Integer progress;
    @TableField(exist = false)
    private String tags;

    /** 是否对外公开：0-仅成员可见，1-所有人可浏览 */
    private Boolean isPublic;

    @TableField(exist = false)
    private String leaderName;
    @TableField(exist = false)
    private String leaderEmail;
    /** 申请人姓名（立项审核列表用，create_by 对应用户） */
    @TableField(exist = false)
    private String applicantName;
    /** 成员数（列表查询时统计） */
    @TableField(exist = false)
    private Integer memberCount;

    /** 立项审核意见 */
    @TableField("audit_opinion")
    private String auditOpinion;
    /** 立项审核时间 */
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    /** 立项审核人 user_id */
    @TableField("audit_by_id")
    private Long auditById;
}
