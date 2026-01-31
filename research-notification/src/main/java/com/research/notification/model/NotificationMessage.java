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

    // 业务通知类型
    public static class NotificationType {
        public static final String TASK_ASSIGNED = "TASK_ASSIGNED";
        public static final String TASK_UPDATED = "TASK_UPDATED";
        public static final String TASK_COMPLETED = "TASK_COMPLETED";
        public static final String PROJECT_INVITED = "PROJECT_INVITED";
        public static final String DOCUMENT_SHARED = "DOCUMENT_SHARED";
        public static final String COMMENT_REPLIED = "COMMENT_REPLIED";
        public static final String SYSTEM_ANNOUNCEMENT = "SYSTEM_ANNOUNCEMENT";
        public static final String DEADLINE_REMINDER = "DEADLINE_REMINDER";
    }
}
