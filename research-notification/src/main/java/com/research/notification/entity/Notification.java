package com.research.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体
 * <p>
 * 兼容原有字段的基础上，增加业务维度与联动动作等扩展字段，
 * 支持按项目/业务类型筛选以及审批/跳转等联动能力。
 */
@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 旧版类型字段（task/document/system），为兼容保留
     */
    private String type;

    /**
     * 业务维度：PROJECT/TASK/DOCUMENT/RESULT/SYSTEM
     */
    private String bizType;

    /**
     * 业务主键ID（如 projectId/taskId/documentId/resultId）
     */
    private Long bizId;

    /**
     * 所属项目ID（便于按项目筛选与权限判断）
     */
    private Long projectId;

    /**
     * 优先级：LOW/NORMAL/HIGH/CRITICAL
     */
    private String priority;

    /**
     * 联动动作类型：APPROVAL/VIEW_DETAIL/OPEN_PUBLIC_PAGE 等
     */
    private String actionType;

    /**
     * 扩展信息（JSON 字符串，存通知参数、审批类型、跳转路由等）
     */
    private String extra;

    private String title;
    private String content;

    /**
     * 0-未读 1-已读
     */
    private Integer readFlag;

    private LocalDateTime sendTime;

    /**
     * 0-正常 1-删除
     */
    private Integer delFlag;
}
