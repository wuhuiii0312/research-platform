package com.research.notification.handler;

import cn.hutool.json.JSONUtil;
import com.research.notification.model.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationWebSocketHandler implements WebSocketHandler {

    // 保存用户ID和WebSocketSession的映射
    private static final ConcurrentHashMap<String, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();

    // 保存sessionId和用户ID的映射
    private static final ConcurrentHashMap<String, String> SESSION_USER_MAP = new ConcurrentHashMap<>();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从session属性中获取用户ID
        Map<String, Object> attributes = session.getAttributes();
        String userId = (String) attributes.get("userId");

        if (userId != null) {
            USER_SESSIONS.put(userId, session);
            SESSION_USER_MAP.put(session.getId(), userId);

            log.info("WebSocket连接建立: userId={}, sessionId={}, 当前在线用户: {}",
                    userId, session.getId(), USER_SESSIONS.size());

            // 发送连接成功消息
            sendMessage(session, NotificationMessage.connected());

            // 发送未读通知
            sendUnreadNotifications(userId, session);
        } else {
            log.warn("WebSocket连接建立失败: 未获取到用户ID");
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            log.debug("收到WebSocket消息: {}", payload);

            try {
                NotificationMessage msg = JSONUtil.toBean(payload, NotificationMessage.class);
                handleMessage(session, msg);
            } catch (Exception e) {
                log.error("解析WebSocket消息失败: {}", e.getMessage());
                sendMessage(session, NotificationMessage.error("消息格式错误"));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        String userId = SESSION_USER_MAP.remove(sessionId);

        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("WebSocket连接关闭: userId={}, sessionId={}, 当前在线用户: {}",
                    userId, sessionId, USER_SESSIONS.size());
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 处理不同类型的消息
     */
    private void handleMessage(WebSocketSession session, NotificationMessage message) throws IOException {
        switch (message.getType()) {
            case "PING":
                // 心跳检测
                sendMessage(session, NotificationMessage.pong());
                break;

            case "SUBSCRIBE":
                // 订阅通知
                handleSubscribe(session, message);
                break;

            case "MARK_READ":
                // 标记已读
                handleMarkRead(session, message);
                break;

            default:
                log.warn("未知的消息类型: {}", message.getType());
                sendMessage(session, NotificationMessage.error("未知的消息类型"));
        }
    }

    /**
     * 处理订阅
     */
    private void handleSubscribe(WebSocketSession session, NotificationMessage message) {
        String userId = SESSION_USER_MAP.get(session.getId());
        if (userId == null) return;

        String channel = message.getData().getStr("channel");
        if (channel != null) {
            // 将用户添加到Redis的订阅集合中
            String key = "notification:subscriptions:" + channel;
            redisTemplate.opsForSet().add(key, userId);

            log.info("用户订阅频道: userId={}, channel={}", userId, channel);
            sendMessage(session, NotificationMessage.success("订阅成功"));
        }
    }

    /**
     * 处理标记已读
     */
    private void handleMarkRead(WebSocketSession session, NotificationMessage message) {
        String userId = SESSION_USER_MAP.get(session.getId());
        if (userId == null) return;

        String notificationId = message.getData().getStr("notificationId");
        if (notificationId != null) {
            // 更新数据库中的通知状态为已读
            // notificationService.markAsRead(userId, notificationId);

            log.info("标记通知已读: userId={}, notificationId={}", userId, notificationId);
            sendMessage(session, NotificationMessage.success("标记已读成功"));
        }
    }

    /**
     * 发送消息给指定用户
     */
    public void sendToUser(String userId, NotificationMessage message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        } else {
            // 用户不在线，将通知保存到数据库
            saveOfflineNotification(userId, message);
        }
    }

    /**
     * 发送消息给多个用户
     */
    public void sendToUsers(List<String> userIds, NotificationMessage message) {
        for (String userId : userIds) {
            sendToUser(userId, message);
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(NotificationMessage message) {
        USER_SESSIONS.forEach((userId, session) -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        });
    }

    /**
     * 发送消息给订阅特定频道的用户
     */
    public void sendToChannel(String channel, NotificationMessage message) {
        // 从Redis获取订阅该频道的用户
        String key = "notification:subscriptions:" + channel;
        var members = redisTemplate.opsForSet().members(key);

        if (members != null) {
            for (String userId : members) {
                sendToUser(userId, message);
            }
        }
    }

    /**
     * 发送WebSocket消息
     */
    private void sendMessage(WebSocketSession session, NotificationMessage message) {
        try {
            if (session.isOpen()) {
                String json = JSONUtil.toJsonStr(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 发送未读通知
     */
    private void sendUnreadNotifications(String userId, WebSocketSession session) {
        // 从数据库获取用户的未读通知
        // List<Notification> unreadNotifications = notificationService.getUnreadNotifications(userId);

        // for (Notification notification : unreadNotifications) {
        //     NotificationMessage message = NotificationMessage.notification(
        //         notification.getTitle(),
        //         notification.getContent(),
        //         notification.getType(),
        //         notification.getRelatedId(),
        //         notification.getRelatedType()
        //     );
        //     sendMessage(session, message);
        // }

        log.info("发送未读通知给用户: userId={}", userId);
    }

    /**
     * 保存离线通知
     */
    private void saveOfflineNotification(String userId, NotificationMessage message) {
        // 将通知保存到数据库，等用户上线时再发送
        // notificationService.saveOfflineNotification(userId, message);

        log.info("保存离线通知: userId={}, type={}", userId, message.getType());
    }
}
