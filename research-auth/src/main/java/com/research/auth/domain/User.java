package com.research.auth.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.research.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")  // 数据库表名
public class User extends BaseEntity {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 状态（0-禁用，1-正常）
     */
    private Integer status;
}