package com.research.notification.model;

/**
 * 通知相关枚举常量，使用字符串存储，避免跨服务强耦合。
 */
public interface NotificationEnums {

    /**
     * 业务维度
     */
    interface BizType {
        String PROJECT = "PROJECT";
        String TASK = "TASK";
        String DOCUMENT = "DOCUMENT";
        String RESULT = "RESULT";
        String SYSTEM = "SYSTEM";
    }

    /**
     * 通知优先级
     */
    interface Priority {
        String LOW = "LOW";
        String NORMAL = "NORMAL";
        String HIGH = "HIGH";
        String CRITICAL = "CRITICAL";
    }

    /**
     * 前端联动动作类型
     */
    interface ActionType {
        /**
         * 审批类：前端展示同意/驳回等操作按钮
         */
        String APPROVAL = "APPROVAL";

        /**
         * 普通详情跳转：如任务详情、文档预览、成果详情
         */
        String VIEW_DETAIL = "VIEW_DETAIL";

        /**
         * 跳转至公开页面（访客使用）
         */
        String OPEN_PUBLIC_PAGE = "OPEN_PUBLIC_PAGE";
    }
}

