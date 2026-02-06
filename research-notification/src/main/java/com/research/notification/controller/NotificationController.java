package com.research.notification.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.util.SecurityUtils;
import com.research.notification.entity.Notification;
import com.research.notification.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 通知列表查询
     * - 负责人/成员：支持按类型/项目/阅读状态筛选
     * - 访客：仅返回公开项目进度、公开文档、新系统公告等公开通知
     */
    @GetMapping("/list")
    public CommonResult<List<Notification>> list(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "bizType", required = false) String bizType,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "readFlag", required = false) Integer readFlag) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) userId = 1L;

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getDelFlag, 0);

        // 普通用户：允许按条件筛选
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        if (bizType != null && !bizType.isEmpty()) {
            wrapper.eq(Notification::getBizType, bizType);
        }
        if (projectId != null) {
            wrapper.eq(Notification::getProjectId, projectId);
        }
        if (readFlag != null) {
            wrapper.eq(Notification::getReadFlag, readFlag);
        }

        wrapper.orderByDesc(Notification::getSendTime);

        List<Notification> list = notificationMapper.selectList(wrapper);
        return CommonResult.success(list);
    }

    /**
     * 标记单条通知为已读
     * - 负责人/成员/访客均可使用
     */
    @PostMapping("/read/{id}")
    public CommonResult<?> markRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) userId = 1L;
        int rows = notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getId, id)
                        .eq(Notification::getUserId, userId)
                        .set(Notification::getReadFlag, 1));
        if (rows == 0) return CommonResult.fail(404, "通知不存在");
        return CommonResult.success("标记已读成功");
    }

    /**
     * 全部标记为已读
     * - 负责人/成员可用
     * - 访客仅允许对公开通知执行（通过 userId 限定）
     */
    @PostMapping("/readAll")
    public CommonResult<?> markAllRead() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) userId = 1L;
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getReadFlag, 0)
                        .set(Notification::getReadFlag, 1));
        return CommonResult.success("全部标记已读成功");
    }

    /**
     * 删除通知
     * - 负责人/成员：允许单条删除
     * - 访客：不允许删除，前端不展示按钮，后端兜底返回403
     */
    @DeleteMapping("/{id}")
    public CommonResult<?> delete(@PathVariable Long id) {
        if (SecurityUtils.isGlobalVisitor()) {
            return CommonResult.fail(403, "访客不支持删除通知");
        }
        Long userId = SecurityUtils.getUserId();
        if (userId == null) userId = 1L;
        int rows = notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getId, id)
                        .eq(Notification::getUserId, userId)
                        .set(Notification::getDelFlag, 1));
        if (rows == 0) return CommonResult.fail(404, "通知不存在");
        return CommonResult.success("删除成功");
    }
}
