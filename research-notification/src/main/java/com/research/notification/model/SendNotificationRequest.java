package com.research.notification.model;

import lombok.Data;

import java.util.List;

/**
 * 通用通知发送请求模型
 *
 * 由各业务模块（项目/任务/文档/成果等）在服务内部构造，
 * 由通知服务负责落库 + WebSocket 推送。
 */
@Data
public class SendNotificationRequest {

    /**
     * 接收人ID列表（必填）
     */
    private List<Long> targetUserIds;

    /**
     * 通知标题（必填）
     */
    private String title;

    /**
     * 通知内容（必填）
     */
    private String content;

    /**
     * 业务维度：PROJECT/TASK/DOCUMENT/RESULT/SYSTEM（必填）
     */
    private String bizType;

    /**
     * 业务主键ID（如 projectId/taskId/documentId/resultId，可选）
     */
    private Long bizId;

    /**
     * 所属项目ID（可选）
     */
    private Long projectId;

    /**
     * 优先级：LOW/NORMAL/HIGH/CRITICAL（默认 NORMAL）
     */
    private String priority;

    /**
     * 联动动作：APPROVAL/VIEW_DETAIL/OPEN_PUBLIC_PAGE 等
     */
    private String actionType;

    /**
     * 通知业务类型（使用 NotificationMessage.NotificationType 常量），便于前端区分图标/标签
     */
    private String notificationType;

    /**
     * 关联对象ID（字符串形态，便于兼容非数字主键）
     */
    private String relatedId;

    /**
     * 关联对象类型（TASK/PROJECT/DOCUMENT/RESULT/SYSTEM 等）
     */
    private String relatedType;

    /**
     * 扩展参数（JSON 字符串），可放审批类型、路由信息等
     */
    private String extra;
}

