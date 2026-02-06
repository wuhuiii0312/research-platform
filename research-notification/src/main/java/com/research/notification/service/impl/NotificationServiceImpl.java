package com.research.notification.service.impl;

import cn.hutool.json.JSONObject;
import com.research.notification.entity.Notification;
import com.research.notification.handler.NotificationWebSocketHandler;
import com.research.notification.mapper.NotificationMapper;
import com.research.notification.model.NotificationEnums;
import com.research.notification.model.NotificationMessage;
import com.research.notification.model.SendNotificationRequest;
import com.research.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void sendNotification(SendNotificationRequest request) {
        if (request == null || request.getTargetUserIds() == null || request.getTargetUserIds().isEmpty()) {
            log.warn("跳过发送通知，接收人为空: {}", request);
            return;
        }
        String priority = request.getPriority() != null ? request.getPriority() : NotificationEnums.Priority.NORMAL;

        // 1. 按接收人落库
        for (Long userId : request.getTargetUserIds()) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            // 兼容旧字段：按业务维度粗略映射 type
            notification.setType(mapBizTypeToLegacyType(request.getBizType()));
            notification.setBizType(request.getBizType());
            notification.setBizId(request.getBizId());
            notification.setProjectId(request.getProjectId());
            notification.setPriority(priority);
            notification.setActionType(request.getActionType());
            notification.setExtra(request.getExtra());
            notification.setTitle(request.getTitle());
            notification.setContent(request.getContent());
            notification.setReadFlag(0);
            notification.setSendTime(LocalDateTime.now());
            notification.setDelFlag(0);
            notificationMapper.insert(notification);
        }

        // 2. WebSocket 推送
        JSONObject data = new JSONObject();
        data.set("title", request.getTitle());
        data.set("content", request.getContent());
        data.set("notificationType", request.getNotificationType());
        data.set("relatedId", request.getRelatedId());
        data.set("relatedType", request.getRelatedType());
        data.set("bizType", request.getBizType());
        data.set("bizId", request.getBizId());
        data.set("projectId", request.getProjectId());
        data.set("priority", priority);
        data.set("actionType", request.getActionType());
        data.set("extra", request.getExtra());
        data.set("read", false);

        NotificationMessage message = new NotificationMessage(NotificationMessage.TYPE_NOTIFICATION, data);
        for (Long userId : request.getTargetUserIds()) {
            webSocketHandler.sendToUser(userId.toString(), message);
        }
    }

    private String mapBizTypeToLegacyType(String bizType) {
        if (bizType == null) {
            return null;
        }
        switch (bizType) {
            case NotificationEnums.BizType.TASK:
                return "task";
            case NotificationEnums.BizType.DOCUMENT:
                return "document";
            case NotificationEnums.BizType.PROJECT:
            case NotificationEnums.BizType.RESULT:
            case NotificationEnums.BizType.SYSTEM:
            default:
                return "system";
        }
    }

    @Override
    public void sendTaskAssignedNotification(Long taskId, String taskName,
                                             Long assigneeId, String assigneeName) {
        String title = "新任务分配";
        String content = String.format("您被分配到新任务：%s", taskName);

        // 持久化 + WebSocket
        SendNotificationRequest request = new SendNotificationRequest();
        request.setTargetUserIds(Arrays.asList(assigneeId));
        request.setTitle(title);
        request.setContent(content);
        request.setBizType(NotificationEnums.BizType.TASK);
        request.setBizId(taskId);
        request.setPriority(NotificationEnums.Priority.HIGH);
        request.setActionType(NotificationEnums.ActionType.VIEW_DETAIL);
        request.setNotificationType(NotificationMessage.NotificationType.TASK_ASSIGNED);
        request.setRelatedId(taskId.toString());
        request.setRelatedType("TASK");
        sendNotification(request);

        // 邮件通知
        sendEmailNotification(assigneeId, title, content);

        log.info("发送任务分配通知: taskId={}, assigneeId={}", taskId, assigneeId);
    }

    @Override
    public void sendTaskUpdatedNotification(Long taskId, String taskName,
                                            Long updaterId, String updaterName,
                                            List<Long> relatedUserIds) {
        String title = "任务更新";
        String content = String.format("任务【%s】已被%s更新", taskName, updaterName);

        SendNotificationRequest request = new SendNotificationRequest();
        request.setTargetUserIds(relatedUserIds);
        request.setTitle(title);
        request.setContent(content);
        request.setBizType(NotificationEnums.BizType.TASK);
        request.setBizId(taskId);
        request.setPriority(NotificationEnums.Priority.NORMAL);
        request.setActionType(NotificationEnums.ActionType.VIEW_DETAIL);
        request.setNotificationType(NotificationMessage.NotificationType.TASK_UPDATED);
        request.setRelatedId(taskId.toString());
        request.setRelatedType("TASK");
        sendNotification(request);

        log.info("发送任务更新通知: taskId={}, relatedUsers={}", taskId, relatedUserIds);
    }

    @Override
    public void sendProjectInvitationNotification(Long projectId, String projectName,
                                                  Long inviterId, String inviterName,
                                                  Long inviteeId, String inviteeEmail) {
        String title = "项目邀请";
        String content = String.format("您被%s邀请加入项目【%s】", inviterName, projectName);

        SendNotificationRequest request = new SendNotificationRequest();
        request.setTargetUserIds(Arrays.asList(inviteeId));
        request.setTitle(title);
        request.setContent(content);
        request.setBizType(NotificationEnums.BizType.PROJECT);
        request.setBizId(projectId);
        request.setPriority(NotificationEnums.Priority.NORMAL);
        request.setActionType(NotificationEnums.ActionType.VIEW_DETAIL);
        request.setNotificationType(NotificationMessage.NotificationType.PROJECT_INVITED);
        request.setRelatedId(projectId.toString());
        request.setRelatedType("PROJECT");
        sendNotification(request);

        // 邮件通知
        sendEmailNotification(inviteeId, title, content);

        log.info("发送项目邀请通知: projectId={}, inviteeId={}", projectId, inviteeId);
    }

    @Override
    public void sendDocumentSharedNotification(Long documentId, String documentName,
                                               Long sharerId, String sharerName,
                                               List<Long> sharedUserIds) {
        String title = "文档共享";
        String content = String.format("%s与您共享了文档【%s】", sharerName, documentName);

        SendNotificationRequest request = new SendNotificationRequest();
        request.setTargetUserIds(sharedUserIds);
        request.setTitle(title);
        request.setContent(content);
        request.setBizType(NotificationEnums.BizType.DOCUMENT);
        request.setBizId(documentId);
        request.setPriority(NotificationEnums.Priority.NORMAL);
        request.setActionType(NotificationEnums.ActionType.VIEW_DETAIL);
        request.setNotificationType(NotificationMessage.NotificationType.DOCUMENT_SHARED);
        request.setRelatedId(documentId.toString());
        request.setRelatedType("DOCUMENT");
        sendNotification(request);

        log.info("发送文档共享通知: documentId={}, sharedUsers={}", documentId, sharedUserIds);
    }

    @Override
    public void sendCommentReplyNotification(Long commentId, Long replyId,
                                             String commentContent, String replyContent,
                                             Long commenterId, Long replierId, String replierName) {
        String title = "评论回复";
        String content = String.format("%s回复了您的评论：%s", replierName, replyContent);

        SendNotificationRequest request = new SendNotificationRequest();
        request.setTargetUserIds(Arrays.asList(commenterId));
        request.setTitle(title);
        request.setContent(content);
        request.setBizType(NotificationEnums.BizType.SYSTEM);
        request.setBizId(commentId);
        request.setPriority(NotificationEnums.Priority.NORMAL);
        request.setActionType(NotificationEnums.ActionType.VIEW_DETAIL);
        request.setNotificationType(NotificationMessage.NotificationType.COMMENT_REPLIED);
        request.setRelatedId(replyId.toString());
        request.setRelatedType("COMMENT");
        sendNotification(request);

        log.info("发送评论回复通知: commentId={}, replierId={}", commentId, replierId);
    }

    @Override
    public void sendSystemAnnouncement(String title, String content, List<Long> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            log.warn("系统公告当前实现仅支持指定用户推送，targetUserIds 为空时跳过发送: title={}", title);
            return;
        }

        SendNotificationRequest request = new SendNotificationRequest();
        request.setTargetUserIds(targetUserIds);
        request.setTitle(title);
        request.setContent(content);
        request.setBizType(NotificationEnums.BizType.SYSTEM);
        request.setPriority(NotificationEnums.Priority.NORMAL);
        request.setActionType(NotificationEnums.ActionType.VIEW_DETAIL);
        request.setNotificationType(NotificationMessage.NotificationType.SYSTEM_ANNOUNCEMENT);
        request.setRelatedType("SYSTEM");
        sendNotification(request);

        log.info("发送系统公告: title={}, targetUsers={}", title, targetUserIds);
    }

    @Override
    public void sendDeadlineReminder(Long taskId, String taskName, Long assigneeId,
                                     String deadline, Integer daysLeft) {
        String title = "截止日期提醒";
        String content = String.format("任务【%s】还有%d天截止（%s）", taskName, daysLeft, deadline);

        SendNotificationRequest request = new SendNotificationRequest();
        request.setTargetUserIds(Arrays.asList(assigneeId));
        request.setTitle(title);
        request.setContent(content);
        request.setBizType(NotificationEnums.BizType.TASK);
        request.setBizId(taskId);
        request.setPriority(NotificationEnums.Priority.HIGH);
        request.setActionType(NotificationEnums.ActionType.VIEW_DETAIL);
        request.setNotificationType(NotificationMessage.NotificationType.DEADLINE_REMINDER);
        request.setRelatedId(taskId.toString());
        request.setRelatedType("TASK");
        // 在 extra 中附带剩余天数与截止日期
        JSONObject extra = new JSONObject();
        extra.set("deadline", deadline);
        extra.set("daysLeft", daysLeft);
        request.setExtra(extra.toString());
        sendNotification(request);

        log.info("发送截止日期提醒: taskId={}, assigneeId={}, daysLeft={}",
                taskId, assigneeId, daysLeft);
    }

    /**
     * 发送邮件通知
     */
    private void sendEmailNotification(Long userId, String title, String content) {
        if (mailSender == null) {
            // 未配置邮件发送时直接跳过，不影响主流程
            log.debug("未配置 JavaMailSender，跳过邮件通知: userId={}, title={}", userId, title);
            return;
        }
        try {
            // 根据用户ID获取邮箱
            String email = getUserEmail(userId);
            if (email == null) return;

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("research-platform@example.com");
            helper.setTo(email);
            helper.setSubject("[科研协作平台] " + title);

            String htmlContent = buildEmailContent(title, content);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            log.info("发送邮件通知成功: userId={}, email={}", userId, email);

        } catch (MessagingException e) {
            log.error("发送邮件通知失败: userId={}", userId, e);
        }
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String title, String content) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #1989fa; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; }
                    .btn { display: inline-block; padding: 10px 20px; background-color: #1989fa; 
                           color: white; text-decoration: none; border-radius: 4px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>科研协作平台</h1>
                    </div>
                    <div class="content">
                        <h2>%s</h2>
                        <p>%s</p>
                        <p style="text-align: center; margin-top: 30px;">
                            <a href="http://localhost:8081" class="btn">前往平台查看</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>此邮件由科研协作平台系统自动发送，请勿回复</p>
                        <p>© 2025 科研协作平台. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, title, title, content);
    }

    /**
     * 获取用户邮箱（模拟）
     */
    private String getUserEmail(Long userId) {
        // TODO: 从数据库查询用户邮箱
        return "user" + userId + "@example.com";
    }
}