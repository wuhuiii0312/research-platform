package com.research.notification.service.impl;

import com.research.notification.handler.NotificationWebSocketHandler;
import com.research.notification.model.NotificationMessage;
import com.research.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendTaskAssignedNotification(Long taskId, String taskName,
                                             Long assigneeId, String assigneeName) {
        String title = "新任务分配";
        String content = String.format("您被分配到新任务：%s", taskName);

        NotificationMessage message = NotificationMessage.notification(
                title, content,
                NotificationMessage.NotificationType.TASK_ASSIGNED,
                taskId.toString(), "TASK"
        );

        // WebSocket通知
        webSocketHandler.sendToUser(assigneeId.toString(), message);

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

        NotificationMessage message = NotificationMessage.notification(
                title, content,
                NotificationMessage.NotificationType.TASK_UPDATED,
                taskId.toString(), "TASK"
        );

        // 发送给相关用户
        for (Long userId : relatedUserIds) {
            webSocketHandler.sendToUser(userId.toString(), message);
        }

        log.info("发送任务更新通知: taskId={}, relatedUsers={}", taskId, relatedUserIds);
    }

    @Override
    public void sendProjectInvitationNotification(Long projectId, String projectName,
                                                  Long inviterId, String inviterName,
                                                  Long inviteeId, String inviteeEmail) {
        String title = "项目邀请";
        String content = String.format("您被%s邀请加入项目【%s】", inviterName, projectName);

        NotificationMessage message = NotificationMessage.notification(
                title, content,
                NotificationMessage.NotificationType.PROJECT_INVITED,
                projectId.toString(), "PROJECT"
        );

        // WebSocket通知
        webSocketHandler.sendToUser(inviteeId.toString(), message);

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

        NotificationMessage message = NotificationMessage.notification(
                title, content,
                NotificationMessage.NotificationType.DOCUMENT_SHARED,
                documentId.toString(), "DOCUMENT"
        );

        // 发送给所有被共享的用户
        for (Long userId : sharedUserIds) {
            webSocketHandler.sendToUser(userId.toString(), message);
        }

        log.info("发送文档共享通知: documentId={}, sharedUsers={}", documentId, sharedUserIds);
    }

    @Override
    public void sendCommentReplyNotification(Long commentId, Long replyId,
                                             String commentContent, String replyContent,
                                             Long commenterId, Long replierId, String replierName) {
        String title = "评论回复";
        String content = String.format("%s回复了您的评论：%s", replierName, replyContent);

        NotificationMessage message = NotificationMessage.notification(
                title, content,
                NotificationMessage.NotificationType.COMMENT_REPLIED,
                replyId.toString(), "COMMENT"
        );

        // 发送给评论者
        webSocketHandler.sendToUser(commenterId.toString(), message);

        log.info("发送评论回复通知: commentId={}, replierId={}", commentId, replierId);
    }

    @Override
    public void sendSystemAnnouncement(String title, String content, List<Long> targetUserIds) {
        NotificationMessage message = NotificationMessage.notification(
                title, content,
                NotificationMessage.NotificationType.SYSTEM_ANNOUNCEMENT,
                null, "SYSTEM"
        );

        if (targetUserIds == null || targetUserIds.isEmpty()) {
            // 广播给所有用户
            webSocketHandler.broadcast(message);
        } else {
            // 发送给指定用户
            for (Long userId : targetUserIds) {
                webSocketHandler.sendToUser(userId.toString(), message);
            }
        }

        log.info("发送系统公告: title={}, targetUsers={}", title, targetUserIds);
    }

    @Override
    public void sendDeadlineReminder(Long taskId, String taskName, Long assigneeId,
                                     String deadline, Integer daysLeft) {
        String title = "截止日期提醒";
        String content = String.format("任务【%s】还有%d天截止（%s）", taskName, daysLeft, deadline);

        NotificationMessage message = NotificationMessage.notification(
                title, content,
                NotificationMessage.NotificationType.DEADLINE_REMINDER,
                taskId.toString(), "TASK"
        );

        webSocketHandler.sendToUser(assigneeId.toString(), message);

        log.info("发送截止日期提醒: taskId={}, assigneeId={}, daysLeft={}",
                taskId, assigneeId, daysLeft);
    }

    /**
     * 发送邮件通知
     */
    private void sendEmailNotification(Long userId, String title, String content) {
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