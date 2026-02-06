package com.research.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.research.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 科研项目成果实体
 *
 * 对应表：project_result
 * 字段设计参考 sql/migrate_project_result.sql
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project_result")
public class ProjectResult extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属项目ID */
    private Long projectId;

    /** 成果名称 */
    private String name;

    /** 成果类型：PAPER/PATENT/SOFT/REPORT */
    private String type;

    /** 成果描述 */
    private String description;

    /** 附件地址（本地路径或OSS链接） */
    private String fileUrl;

    /** 状态：PENDING/PASSED/ARCHIVED 等 */
    private String status;

    /** 提交人ID */
    private Long submitterId;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /** 审核人ID */
    private Long auditUserId;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 审核备注 */
    private String auditRemark;

    /** 是否测试数据：0-否/1-是（可选列，对应 test_data_flag） */
    private Integer testDataFlag;
}

