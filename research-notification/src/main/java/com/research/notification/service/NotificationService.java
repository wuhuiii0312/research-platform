package com.research.notification.service;

import com.research.notification.model.NotificationMessage;
import com.research.notification.model.SendNotificationRequest;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 通用通知发送入口：
     * - 负责将通知持久化到 notification 表
     * - 同步通过 WebSocket 推送到在线用户
     * - 预留与邮件/短信等其他通道的扩展点
     */
    void sendNotification(SendNotificationRequest request);

    void sendTaskAssignedNotification(Long taskId, String taskName,
                                      Long assigneeId, String assigneeName);

    void sendTaskUpdatedNotification(Long taskId, String taskName,
                                     Long updaterId, String updaterName,
                                     List<Long> relatedUserIds);

    void sendProjectInvitationNotification(Long projectId, String projectName,
                                           Long inviterId, String inviterName,
                                           Long inviteeId, String inviteeEmail);

    void sendDocumentSharedNotification(Long documentId, String documentName,
                                        Long sharerId, String sharerName,
                                        List<Long> sharedUserIds);

    void sendCommentReplyNotification(Long commentId, Long replyId,
                                      String commentContent, String replyContent,
                                      Long commenterId, Long replierId, String replierName);

    void sendSystemAnnouncement(String title, String content, List<Long> targetUserIds);

    void sendDeadlineReminder(Long taskId, String taskName, Long assigneeId,
                              String deadline, Integer daysLeft);
}
