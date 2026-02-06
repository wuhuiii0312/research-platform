package com.research.notification.model;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationMessage {
    private String type;        // 消息类型
    private JSONObject data;    // 消息数据
    private LocalDateTime timestamp; // 时间戳

    // 消息类型常量
    public static final String TYPE_CONNECTED = "CONNECTED";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_SUCCESS = "SUCCESS";
    public static final String TYPE_NOTIFICATION = "NOTIFICATION";
    public static final String TYPE_PING = "PING";
    public static final String TYPE_PONG = "PONG";

    public NotificationMessage() {
        this.timestamp = LocalDateTime.now();
        this.data = new JSONObject();
    }

    public NotificationMessage(String type) {
        this();
        this.type = type;
    }

    public NotificationMessage(String type, JSONObject data) {
        this();
        this.type = type;
        this.data = data;
    }

    // 静态工厂方法

    public static NotificationMessage connected() {
        JSONObject data = new JSONObject();
        data.set("message", "连接成功");
        data.set("timestamp", LocalDateTime.now().toString());
        return new NotificationMessage(TYPE_CONNECTED, data);
    }

    public static NotificationMessage error(String message) {
        JSONObject data = new JSONObject();
        data.set("message", message);
        return new NotificationMessage(TYPE_ERROR, data);
    }

    public static NotificationMessage success(String message) {
        JSONObject data = new JSONObject();
        data.set("message", message);
        return new NotificationMessage(TYPE_SUCCESS, data);
    }

    public static NotificationMessage notification(String title, String content,
                                                   String notificationType,
                                                   String relatedId, String relatedType) {
        JSONObject data = new JSONObject();
        data.set("title", title);
        data.set("content", content);
        data.set("notificationType", notificationType);
        data.set("relatedId", relatedId);
        data.set("relatedType", relatedType);
        data.set("read", false);
        return new NotificationMessage(TYPE_NOTIFICATION, data);
    }

    public static NotificationMessage ping() {
        return new NotificationMessage(TYPE_PING);
    }

    public static NotificationMessage pong() {
        return new NotificationMessage(TYPE_PONG);
    }

    // 业务通知类型（按项目/任务/文档/成果/系统分组）
    public static class NotificationType {
        // 项目类
        public static final String PROJECT_JOIN_REQUEST = "PROJECT_JOIN_REQUEST";           // 成员加入申请 → 负责人
        public static final String PROJECT_QUIT_REQUEST = "PROJECT_QUIT_REQUEST";           // 成员退出申请 → 负责人
        public static final String PROJECT_INFO_UPDATED = "PROJECT_INFO_UPDATED";           // 项目信息修改 → 负责人+成员
        public static final String PROJECT_ARCHIVE_PROGRESS = "PROJECT_ARCHIVE_PROGRESS";   // 项目归档进度 → 负责人+成员
        public static final String PROJECT_PUBLIC_PROGRESS = "PROJECT_PUBLIC_PROGRESS";     // 公开项目进度 → 访客
        public static final String PROJECT_PUBLIC_DOC_ADDED = "PROJECT_PUBLIC_DOC_ADDED";   // 项目公开文档新增 → 访客
        public static final String PROJECT_INVITED = "PROJECT_INVITED";                     // 项目邀请 → 成员

        // 任务类
        public static final String TASK_ASSIGNED = "TASK_ASSIGNED";                         // 任务分配 → 成员
        public static final String TASK_UPDATED = "TASK_UPDATED";                           // 任务更新 → 相关成员
        public static final String TASK_COMPLETED = "TASK_COMPLETED";                       // 任务完成 → 相关成员
        public static final String TASK_SUBMIT_APPROVAL = "TASK_SUBMIT_APPROVAL";           // 任务提交审批 → 负责人
        public static final String TASK_DELAY_REQUEST = "TASK_DELAY_REQUEST";               // 任务延期申请 → 负责人
        public static final String TASK_HIGH_RISK_ALERT = "TASK_HIGH_RISK_ALERT";           // 高优先级任务异常 → 负责人
        public static final String TASK_DUE_REMINDER = "TASK_DUE_REMINDER";                 // 截止日期预警 → 成员
        public static final String TASK_COMMENTED = "TASK_COMMENTED";                       // 被批注 → 成员
        public static final String TASK_FORWARD = "TASK_FORWARD";                           // 被转发 → 成员
        public static final String TASK_OWNER_CHANGED = "TASK_OWNER_CHANGED";               // 负责人调整任务 → 成员

        // 文档类
        public static final String DOC_UPLOAD_SUCCESS = "DOC_UPLOAD_SUCCESS";               // 文档上传成功 → 成员本人
        public static final String DOC_DELETED_CORE = "DOC_DELETED_CORE";                   // 核心文档删除 → 负责人+相关成员
        public static final String DOC_PERMISSION_CHANGED = "DOC_PERMISSION_CHANGED";       // 文档权限变更 → 受影响负责人/成员
        public static final String DOC_VERSION_UPDATED = "DOC_VERSION_UPDATED";             // 文档版本更新 → 关注者/相关成员
        public static final String DOC_COMMENT_MENTION = "DOC_COMMENT_MENTION";             // 文档批注 @ 提及 → 被提及人
        public static final String DOC_PUBLIC_ADDED = "DOC_PUBLIC_ADDED";                   // 项目公开文档新增 → 访客
        public static final String DOCUMENT_SHARED = "DOCUMENT_SHARED";                     // 文档共享 → 被共享人

        // 成果类
        public static final String RESULT_SUBMIT_SUCCESS = "RESULT_SUBMIT_SUCCESS";         // 成果提交成功 → 成员本人
        public static final String RESULT_REVIEW_REQUEST = "RESULT_REVIEW_REQUEST";         // 成果提交审核 → 负责人
        public static final String RESULT_REVIEW_RESULT = "RESULT_REVIEW_RESULT";           // 审核结果 → 成员
        public static final String RESULT_ARCHIVE_FEEDBACK = "RESULT_ARCHIVE_FEEDBACK";     // 成果归档反馈 → 成员+负责人
        public static final String RESULT_STAT_UPDATED = "RESULT_STAT_UPDATED";             // 成果统计数据更新 → 负责人+成员

        // 系统类
        public static final String SYSTEM_ANNOUNCEMENT = "SYSTEM_ANNOUNCEMENT";             // 平台公告
        public static final String ACCOUNT_SECURITY_ALERT = "ACCOUNT_SECURITY_ALERT";       // 账号安全提醒
        public static final String CONCURRENCY_WARNING = "CONCURRENCY_WARNING";             // 并发访问预警
        public static final String DEADLINE_REMINDER = "DEADLINE_REMINDER";                 // 兼容旧枚举：截止日期提醒
        public static final String VISITOR_PERMISSION_EXPIRE = "VISITOR_PERMISSION_EXPIRE"; // 访客权限到期提醒
        public static final String COMMENT_REPLIED = "COMMENT_REPLIED";                     // 评论回复（保留旧类型）
    }
}
