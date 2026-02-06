package com.research.notification.controller;

import com.research.common.core.domain.CommonResult;
import com.research.notification.model.SendNotificationRequest;
import com.research.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知内部接口（仅供微服务之间调用）
 */
@RestController
@RequestMapping("/internal/notification")
public class NotificationInternalController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public CommonResult<Void> send(@RequestBody SendNotificationRequest request) {
        notificationService.sendNotification(request);
        return CommonResult.success(null);
    }
}

